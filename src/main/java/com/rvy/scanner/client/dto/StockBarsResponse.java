package com.rvy.scanner.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StockBarsResponse {

    private List<OptionBarDto> bars;

    @JsonProperty("next_page_token")
    private String nextPageToken;

    public List<OptionBarDto> getBars() {
        return bars;
    }

    public void setBars(List<OptionBarDto> bars) {
        this.bars = bars;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}
