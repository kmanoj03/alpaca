package com.rvy.scanner.client;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EarningsClient {

    private static final Logger log = LoggerFactory.getLogger(EarningsClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public EarningsClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<LocalDate> nextEarningsDate(String symbol) {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString("https://query2.finance.yahoo.com/v10/finance/quoteSummary/{symbol}")
                    .queryParam("modules", "calendarEvents")
                    .buildAndExpand(symbol)
                    .encode()
                    .toUri();
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 AlpacaOptionsScanner/0.1");
            headers.set("Accept", "application/json");
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (response.getBody() == null) {
                return Optional.empty();
            }
            return parse(response.getBody());
        } catch (RuntimeException ex) {
            log.info("Earnings lookup failed for {}: {}", symbol, ex.getMessage());
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
            log.info("Could not parse earnings JSON: {}", ex.getMessage());
            return Optional.empty();
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
