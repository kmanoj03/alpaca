package com.rvy.scanner.client;

import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.rvy.scanner.client.dto.OptionBarDto;
import com.rvy.scanner.client.dto.OptionChainResponse;
import com.rvy.scanner.client.dto.OptionContractDto;
import com.rvy.scanner.client.dto.OptionContractsResponse;
import com.rvy.scanner.client.dto.OptionGreeksDto;
import com.rvy.scanner.client.dto.OptionQuoteDto;
import com.rvy.scanner.client.dto.OptionSnapshotDto;
import com.rvy.scanner.client.dto.StockBarsResponse;
import com.rvy.scanner.client.dto.StockSnapshotResponse;
import com.rvy.scanner.config.AlpacaProperties;
import com.rvy.scanner.exception.AlpacaApiException;
import com.rvy.scanner.exception.InvalidSymbolException;
import com.rvy.scanner.exception.MissingCredentialsException;
import com.rvy.scanner.exception.MissingDataException;
import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.OptionGreeks;
import com.rvy.scanner.model.OptionQuote;
import com.rvy.scanner.model.StockBar;

@Component
public class AlpacaClient {

    private static final Logger log = LoggerFactory.getLogger(AlpacaClient.class);
    private static final int MAX_PAGES = 50;
    private static final int MAX_RETRIES = 3;

    private final RestTemplate restTemplate;
    private final AlpacaProperties properties;

    public AlpacaClient(RestTemplate restTemplate, AlpacaProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public List<OptionContract> fetchOptionChain(String underlyingSymbol) {
        requireCredentials();
        String symbol = normalizeSymbol(underlyingSymbol);
        List<OptionContract> contracts = new ArrayList<>();
        String pageToken = null;
        int pages = 0;

        do {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(properties.getDataBaseUrl() + "/v1beta1/options/snapshots/{symbol}")
                    .queryParam("limit", properties.getChainPageLimit());
            if (pageToken != null && !pageToken.isBlank()) {
                builder.queryParam("page_token", pageToken);
            }
            URI uri = builder.buildAndExpand(symbol).encode().toUri();
            OptionChainResponse response = get(uri, OptionChainResponse.class);
            if (response == null || response.getSnapshots() == null || response.getSnapshots().isEmpty()) {
                if (pages == 0) {
                    return List.of();
                }
                break;
            }
            for (Map.Entry<String, OptionSnapshotDto> entry : response.getSnapshots().entrySet()) {
                toContract(symbol, entry.getKey(), entry.getValue()).ifPresent(contracts::add);
            }
            pageToken = blankToNull(response.getNextPageToken());
            pages++;
        } while (pageToken != null && pages < MAX_PAGES);

        if (pageToken != null) {
            log.warn("Stopped option-chain pagination for {} after {} pages; more data may exist", symbol, pages);
        }
        return contracts;
    }

    public double fetchUnderlyingPrice(String underlyingSymbol) {
        requireCredentials();
        String symbol = normalizeSymbol(underlyingSymbol);
        URI uri = UriComponentsBuilder
                .fromUriString(properties.getDataBaseUrl() + "/v2/stocks/{symbol}/snapshot")
                .buildAndExpand(symbol)
                .encode()
                .toUri();
        StockSnapshotResponse snapshot;
        try {
            snapshot = get(uri, StockSnapshotResponse.class);
        } catch (AlpacaApiException ex) {
            if (ex.getStatusCode() == 400 || ex.getStatusCode() == 404) {
                throw new InvalidSymbolException(symbol);
            }
            throw ex;
        }
        if (snapshot == null) {
            throw new MissingDataException("Missing underlying price for " + symbol);
        }
        if (snapshot.getLatestTrade() != null && snapshot.getLatestTrade().getP() != null) {
            return snapshot.getLatestTrade().getP();
        }
        if (snapshot.getLatestQuote() != null
                && snapshot.getLatestQuote().getBp() != null
                && snapshot.getLatestQuote().getAp() != null) {
            return (snapshot.getLatestQuote().getBp() + snapshot.getLatestQuote().getAp()) / 2.0;
        }
        throw new MissingDataException("Missing underlying price for " + symbol);
    }

    public Map<String, Long> fetchOpenInterest(String underlyingSymbol) {
        requireCredentials();
        String symbol = normalizeSymbol(underlyingSymbol);
        Map<String, Long> openInterest = new HashMap<>();
        String pageToken = null;
        int pages = 0;

        do {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(properties.getTradeBaseUrl() + "/v2/options/contracts")
                    .queryParam("underlying_symbols", symbol)
                    .queryParam("status", "active")
                    .queryParam("limit", properties.getContractsPageLimit());
            if (pageToken != null) {
                builder.queryParam("page_token", pageToken);
            }
            URI uri = builder.build(true).toUri();
            OptionContractsResponse response = get(uri, OptionContractsResponse.class);
            if (response == null || response.getOptionContracts() == null) {
                break;
            }
            for (OptionContractDto dto : response.getOptionContracts()) {
                Long oi = parseLong(dto.getOpenInterest());
                if (dto.getSymbol() != null && oi != null) {
                    openInterest.put(dto.getSymbol(), oi);
                }
            }
            pageToken = blankToNull(response.getNextPageToken());
            pages++;
        } while (pageToken != null && pages < MAX_PAGES);

        return openInterest;
    }

    public List<StockBar> fetchDailyBars(String underlyingSymbol, LocalDate start, LocalDate end) {
        requireCredentials();
        String symbol = normalizeSymbol(underlyingSymbol);
        List<StockBar> bars = new ArrayList<>();
        String pageToken = null;
        int pages = 0;

        do {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(properties.getDataBaseUrl() + "/v2/stocks/{symbol}/bars")
                    .queryParam("timeframe", "1Day")
                    .queryParam("start", start.toString())
                    .queryParam("end", end.toString())
                    .queryParam("limit", 10000)
                    .queryParam("adjustment", "split")
                    .queryParam("feed", "iex");
            if (pageToken != null) {
                builder.queryParam("page_token", pageToken);
            }
            URI uri = builder.buildAndExpand(symbol).encode().toUri();
            StockBarsResponse response = get(uri, StockBarsResponse.class);
            if (response == null || response.getBars() == null) {
                break;
            }
            for (OptionBarDto dto : response.getBars()) {
                toStockBar(dto).ifPresent(bars::add);
            }
            pageToken = blankToNull(response.getNextPageToken());
            pages++;
        } while (pageToken != null && pages < MAX_PAGES);

        return bars;
    }

    public void applyOpenInterest(List<OptionContract> contracts, Map<String, Long> openInterest) {
        if (contracts == null || openInterest == null) {
            return;
        }
        for (OptionContract contract : contracts) {
            Long oi = openInterest.get(contract.getSymbol());
            if (oi != null) {
                contract.setOpenInterest(oi);
            }
        }
    }

    private Optional<OptionContract> toContract(String underlying, String occSymbol, OptionSnapshotDto snapshot) {
        return OccSymbolParser.parse(occSymbol).map(parsed -> {
            OptionContract contract = new OptionContract();
            contract.setSymbol(occSymbol);
            contract.setUnderlyingSymbol(underlying);
            contract.setType(parsed.type());
            contract.setStrike(parsed.strike());
            contract.setExpiration(parsed.expiration());
            if (snapshot == null) {
                return contract;
            }
            contract.setImpliedVolatility(snapshot.getImpliedVolatility());
            if (snapshot.getLatestTrade() != null) {
                contract.setLatestTradePrice(snapshot.getLatestTrade().getP());
            }
            if (snapshot.getDailyBar() != null && snapshot.getDailyBar().getV() != null) {
                contract.setVolume(snapshot.getDailyBar().getV());
            }
            if (snapshot.getLatestQuote() != null) {
                OptionQuote quote = new OptionQuote();
                quote.setBid(snapshot.getLatestQuote().getBp());
                quote.setAsk(snapshot.getLatestQuote().getAp());
                quote.setBidSize(snapshot.getLatestQuote().getBs());
                quote.setAskSize(snapshot.getLatestQuote().getAs());
                contract.setQuote(quote);
            }
            if (snapshot.getGreeks() != null) {
                OptionGreeksDto dto = snapshot.getGreeks();
                OptionGreeks greeks = new OptionGreeks();
                greeks.setDelta(dto.getDelta());
                greeks.setTheta(dto.getTheta());
                greeks.setGamma(dto.getGamma());
                greeks.setVega(dto.getVega());
                greeks.setRho(dto.getRho());
                contract.setGreeks(greeks);
            }
            return contract;
        });
    }

    private Optional<StockBar> toStockBar(OptionBarDto dto) {
        if (dto == null || dto.getC() == null || dto.getT() == null) {
            return Optional.empty();
        }
        try {
            LocalDate date = OffsetDateTime.parse(dto.getT()).toLocalDate();
            StockBar bar = new StockBar();
            bar.setDate(date);
            bar.setOpen(dto.getO() == null ? dto.getC() : dto.getO());
            bar.setHigh(dto.getH() == null ? dto.getC() : dto.getH());
            bar.setLow(dto.getL() == null ? dto.getC() : dto.getL());
            bar.setClose(dto.getC());
            bar.setVolume(dto.getV() == null ? 0L : dto.getV());
            return Optional.of(bar);
        } catch (DateTimeParseException ex) {
            try {
                StockBar bar = new StockBar();
                bar.setDate(LocalDate.parse(dto.getT().substring(0, 10)));
                bar.setOpen(dto.getO() == null ? dto.getC() : dto.getO());
                bar.setHigh(dto.getH() == null ? dto.getC() : dto.getH());
                bar.setLow(dto.getL() == null ? dto.getC() : dto.getL());
                bar.setClose(dto.getC());
                bar.setVolume(dto.getV() == null ? 0L : dto.getV());
                return Optional.of(bar);
            } catch (RuntimeException ignored) {
                return Optional.empty();
            }
        }
    }

    private <T> T get(URI uri, Class<T> type) {
        HttpStatusCodeException lastRateLimit = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                ResponseEntity<T> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers()), type);
                return response.getBody();
            } catch (HttpStatusCodeException ex) {
                int status = ex.getStatusCode().value();
                if (status == 429 && attempt < MAX_RETRIES) {
                    lastRateLimit = ex;
                    sleepQuietly(200L * attempt);
                    continue;
                }
                if (status == 400) {
                    throw new AlpacaApiException(400, "Invalid Alpaca request: " + trimBody(ex.getResponseBodyAsString()));
                }
                throw new AlpacaApiException(ex.getStatusCode(), trimBody(ex.getResponseBodyAsString()));
            }
        }
        throw new AlpacaApiException(lastRateLimit.getStatusCode(), trimBody(lastRateLimit.getResponseBodyAsString()));
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("APCA-API-KEY-ID", properties.getApiKey());
        headers.set("APCA-API-SECRET-KEY", properties.getApiSecret());
        headers.set("Accept", "application/json");
        return headers;
    }

    private void requireCredentials() {
        if (!properties.hasCredentials()) {
            throw new MissingCredentialsException();
        }
    }

    private static String normalizeSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new InvalidSymbolException("(empty)");
        }
        return symbol.trim().toUpperCase();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.replace(",", "").trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String trimBody(String body) {
        if (body == null) {
            return "";
        }
        String trimmed = body.replaceAll("\\s+", " ").trim();
        return trimmed.length() > 300 ? trimmed.substring(0, 300) : trimmed;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
