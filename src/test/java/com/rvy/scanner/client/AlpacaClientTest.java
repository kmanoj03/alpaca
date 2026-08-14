package com.rvy.scanner.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.rvy.scanner.config.AlpacaProperties;
import com.rvy.scanner.exception.AlpacaApiException;
import com.rvy.scanner.exception.InvalidSymbolException;
import com.rvy.scanner.exception.MissingCredentialsException;
import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.OptionType;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

class AlpacaClientTest {

    private MockWebServer server;
    private AlpacaClient client;
    private AlpacaProperties properties;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        properties = new AlpacaProperties();
        properties.setDataBaseUrl(server.url("/").toString().replaceAll("/$", ""));
        properties.setTradeBaseUrl(server.url("/").toString().replaceAll("/$", ""));
        properties.setApiKey("test-key");
        properties.setApiSecret("test-secret");
        properties.setChainPageLimit(1000);
        properties.setContractsPageLimit(10000);
        client = new AlpacaClient(new RestTemplate(new SimpleClientHttpRequestFactory()), properties);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void fetchOptionChainMapsSnapshotAndPaginates() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "next_page_token": "page-2",
                          "snapshots": {
                            "SPY240821C00680000": {
                              "greeks": { "delta": 0.08, "theta": -0.018, "gamma": 0.01, "vega": 0.2, "rho": 0.01 },
                              "impliedVolatility": 0.22,
                              "latestQuote": { "bp": 0.35, "ap": 0.40, "bs": 10, "as": 12 },
                              "latestTrade": { "p": 0.37 },
                              "dailyBar": { "v": 1500 }
                            }
                          }
                        }
                        """));
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "next_page_token": null,
                          "snapshots": {
                            "SPY240821P00625000": {
                              "greeks": { "delta": -0.07, "theta": -0.016, "gamma": 0.01, "vega": 0.2, "rho": 0.01 },
                              "latestQuote": { "bp": 0.28, "ap": 0.32, "bs": 8, "as": 9 }
                            }
                          }
                        }
                        """));

        List<OptionContract> contracts = client.fetchOptionChain("spy");

        assertThat(contracts).hasSize(2);
        OptionContract call = contracts.stream().filter(OptionContract::isCall).findFirst().orElseThrow();
        assertThat(call.getStrike()).isEqualTo(680.0);
        assertThat(call.getDelta()).isEqualTo(0.08);
        assertThat(call.getVolume()).isEqualTo(1500);
        assertThat(call.getMid()).isEqualTo(0.375);

        OptionContract put = contracts.stream().filter(OptionContract::isPut).findFirst().orElseThrow();
        assertThat(put.getType()).isEqualTo(OptionType.PUT);
        assertThat(put.getStrike()).isEqualTo(625.0);

        RecordedRequest first = server.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest second = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(first.getHeader("APCA-API-KEY-ID")).isEqualTo("test-key");
        assertThat(first.getPath()).contains("/v1beta1/options/snapshots/SPY");
        assertThat(second.getRequestUrl().queryParameter("page_token")).isEqualTo("page-2");
    }

    @Test
    void fetchOptionChainReturnsEmptyWhenNoSnapshots() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"next_page_token\": null, \"snapshots\": {}}"));

        assertThat(client.fetchOptionChain("SPY")).isEmpty();
    }

    @Test
    void fetchOptionChainThrowsOnUnauthorized() {
        server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"message\":\"unauthorized\"}"));

        assertThatThrownBy(() -> client.fetchOptionChain("SPY"))
                .isInstanceOf(AlpacaApiException.class)
                .hasMessageContaining("credentials")
                .extracting(ex -> ((AlpacaApiException) ex).getStatusCode())
                .isEqualTo(401);
    }

    @Test
    void fetchOptionChainThrowsOnInvalidRequest() {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("{\"message\":\"bad symbol\"}"));

        assertThatThrownBy(() -> client.fetchOptionChain("SPY"))
                .isInstanceOf(AlpacaApiException.class)
                .extracting(ex -> ((AlpacaApiException) ex).getStatusCode())
                .isEqualTo(400);
    }

    @Test
    void fetchOptionChainThrowsAfterRateLimitRetries() {
        server.enqueue(new MockResponse().setResponseCode(429).setBody("rate"));
        server.enqueue(new MockResponse().setResponseCode(429).setBody("rate"));
        server.enqueue(new MockResponse().setResponseCode(429).setBody("rate"));

        assertThatThrownBy(() -> client.fetchOptionChain("SPY"))
                .isInstanceOf(AlpacaApiException.class)
                .extracting(ex -> ((AlpacaApiException) ex).getStatusCode())
                .isEqualTo(429);
    }

    @Test
    void missingCredentialsAreRejectedBeforeHttp() {
        properties.setApiKey("");
        properties.setApiSecret("");

        assertThatThrownBy(() -> client.fetchOptionChain("SPY"))
                .isInstanceOf(MissingCredentialsException.class);
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void fetchUnderlyingPriceUsesLastTrade() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"latestTrade\": {\"p\": 650.25}, \"latestQuote\": {\"bp\": 650.2, \"ap\": 650.3}}"));

        assertThat(client.fetchUnderlyingPrice("SPY")).isEqualTo(650.25);
    }

    @Test
    void fetchUnderlyingPriceMapsBadSymbol() {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("invalid"));

        assertThatThrownBy(() -> client.fetchUnderlyingPrice("ZZZZ"))
                .isInstanceOf(InvalidSymbolException.class);
    }

    @Test
    void fetchDailyBarsUsesIexFeedForPaperAccounts() throws Exception {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "bars": [
                            { "t": "2026-08-11T04:00:00Z", "o": 770.0, "h": 773.0, "l": 768.0, "c": 772.5, "v": 1000 }
                          ],
                          "next_page_token": null
                        }
                        """));

        assertThat(client.fetchDailyBars("SPY", java.time.LocalDate.of(2026, 8, 1), java.time.LocalDate.of(2026, 8, 12)))
                .hasSize(1)
                .first()
                .extracting(com.rvy.scanner.model.StockBar::getClose)
                .isEqualTo(772.5);

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request.getRequestUrl().queryParameter("feed")).isEqualTo("iex");
        assertThat(request.getPath()).contains("/v2/stocks/SPY/bars");
    }

    @Test
    void fetchOpenInterestJoinsByOccSymbol() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "option_contracts": [
                            { "symbol": "SPY240821C00680000", "open_interest": "1200" }
                          ],
                          "next_page_token": null
                        }
                        """));

        Map<String, Long> oi = client.fetchOpenInterest("SPY");
        assertThat(oi).containsEntry("SPY240821C00680000", 1200L);
    }
}
