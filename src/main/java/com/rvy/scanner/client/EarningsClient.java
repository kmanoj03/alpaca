package com.rvy.scanner.client;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EarningsClient {

    private static final Logger log = LoggerFactory.getLogger(EarningsClient.class);
    private static final Duration CRUMB_TTL = Duration.ofHours(1);
    private static final Duration SKIP_TTL = Duration.ofMinutes(15);
    private static final String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, String> cookies = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> skipUntil = new ConcurrentHashMap<>();

    private volatile String crumb;
    private volatile Instant crumbAt;
    private volatile boolean warned;

    public EarningsClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<LocalDate> nextEarningsDate(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }
        Instant skip = skipUntil.get(symbol.toUpperCase());
        if (skip != null && Instant.now().isBefore(skip)) {
            return Optional.empty();
        }
        try {
            String token = crumb();
            if (token == null) {
                rememberFailure(symbol, "Yahoo crumb unavailable");
                return Optional.empty();
            }
            URI uri = UriComponentsBuilder
                    .fromUriString("https://query2.finance.yahoo.com/v10/finance/quoteSummary/{symbol}")
                    .queryParam("modules", "calendarEvents")
                    .queryParam("crumb", token)
                    .buildAndExpand(symbol)
                    .encode()
                    .toUri();
            ResponseEntity<String> response = restTemplate.exchange(
                    uri, HttpMethod.GET, new HttpEntity<>(yahooHeaders()), String.class);
            collectCookies(response.getHeaders());
            if (response.getBody() == null) {
                return Optional.empty();
            }
            return parse(response.getBody());
        } catch (RuntimeException ex) {
            rememberFailure(symbol, ex.getMessage());
            return Optional.empty();
        }
    }

    Optional<LocalDate> parse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode dates = root.path("quoteSummary").path("result");
            if (!dates.isArray() || dates.isEmpty()) {
                return Optional.empty();
            }
            JsonNode earningsDate = dates.get(0).path("calendarEvents").path("earnings").path("earningsDate");
            if (earningsDate.isArray()) {
                for (JsonNode node : earningsDate) {
                    Optional<LocalDate> parsed = fromNode(node);
                    if (parsed.isPresent()) {
                        return parsed;
                    }
                }
            }
            return fromNode(earningsDate);
        } catch (Exception ex) {
            log.debug("Could not parse earnings JSON: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private String crumb() {
        if (crumb != null && crumbAt != null && Instant.now().isBefore(crumbAt.plus(CRUMB_TTL))) {
            return crumb;
        }
        synchronized (this) {
            if (crumb != null && crumbAt != null && Instant.now().isBefore(crumbAt.plus(CRUMB_TTL))) {
                return crumb;
            }
            warmup();
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        URI.create("https://query1.finance.yahoo.com/v1/test/getcrumb"),
                        HttpMethod.GET,
                        new HttpEntity<>(yahooHeaders()),
                        String.class);
                collectCookies(response.getHeaders());
                String body = response.getBody() == null ? "" : response.getBody().trim();
                if (!body.isEmpty() && !body.contains(" ") && !body.toLowerCase().contains("too many")) {
                    crumb = body;
                    crumbAt = Instant.now();
                    return crumb;
                }
            } catch (HttpStatusCodeException ex) {
                collectCookies(ex.getResponseHeaders());
            } catch (RuntimeException ignored) {
                // fail open
            }
            return null;
        }
    }

    private void warmup() {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    URI.create("https://fc.yahoo.com"),
                    HttpMethod.GET,
                    new HttpEntity<>(yahooHeaders()),
                    String.class);
            collectCookies(response.getHeaders());
        } catch (HttpStatusCodeException ex) {
            collectCookies(ex.getResponseHeaders());
        } catch (RuntimeException ignored) {
            // fc.yahoo.com often 404s after setting cookies
        }
    }

    private void rememberFailure(String symbol, String reason) {
        skipUntil.put(symbol.toUpperCase(), Instant.now().plus(SKIP_TTL));
        if (!warned) {
            warned = true;
            log.warn("Earnings lookup unavailable (bonus flag skipped): {}", reason);
        } else {
            log.debug("Earnings lookup skipped for {}: {}", symbol, reason);
        }
    }

    private HttpHeaders yahooHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);
        headers.set("Accept", "application/json,text/plain,*/*");
        if (!cookies.isEmpty()) {
            headers.set(HttpHeaders.COOKIE, cookies.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("; ")));
        }
        return headers;
    }

    private void collectCookies(HttpHeaders headers) {
        if (headers == null) {
            return;
        }
        List<String> setCookie = headers.get(HttpHeaders.SET_COOKIE);
        if (setCookie == null) {
            return;
        }
        for (String cookie : setCookie) {
            String pair = cookie.split(";", 2)[0];
            int eq = pair.indexOf('=');
            if (eq > 0) {
                cookies.put(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
            }
        }
    }

    private Optional<LocalDate> fromNode(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return Optional.empty();
        }
        if (node.hasNonNull("fmt")) {
            try {
                return Optional.of(LocalDate.parse(node.get("fmt").asText().substring(0, 10)));
            } catch (RuntimeException ignored) {
                // fall through to raw
            }
        }
        if (node.hasNonNull("raw")) {
            long raw = node.get("raw").asLong();
            if (raw > 0) {
                return Optional.of(Instant.ofEpochSecond(raw).atZone(ZoneOffset.UTC).toLocalDate());
            }
        }
        if (node.isTextual()) {
            try {
                return Optional.of(LocalDate.parse(node.asText().substring(0, 10)));
            } catch (RuntimeException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
