package com.rvy.scanner.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

class EarningsClientTest {

    private final EarningsClient client = new EarningsClient(new RestTemplate(), new ObjectMapper());

    @Test
    void parsesYahooCalendarEvents() {
        String json = """
                {
                  "quoteSummary": {
                    "result": [{
                      "calendarEvents": {
                        "earnings": {
                          "earningsDate": [{ "raw": 1786320000, "fmt": "2026-08-10" }]
                        }
                      }
                    }]
                  }
                }
                """;
        Optional<LocalDate> date = client.parse(json);
        assertThat(date).contains(LocalDate.of(2026, 8, 10));
    }

    @Test
    void emptyResultIsSafe() {
        assertThat(client.parse("{\"quoteSummary\":{\"result\":[]}}")).isEmpty();
    }
}
