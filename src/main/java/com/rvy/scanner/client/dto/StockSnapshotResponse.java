package com.rvy.scanner.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StockSnapshotResponse {

    private OptionTradeDto latestTrade;
    private OptionQuoteDto latestQuote;

    public OptionTradeDto getLatestTrade() {
        return latestTrade;
    }

    public void setLatestTrade(OptionTradeDto latestTrade) {
        this.latestTrade = latestTrade;
    }

    public OptionQuoteDto getLatestQuote() {
        return latestQuote;
    }

    public void setLatestQuote(OptionQuoteDto latestQuote) {
        this.latestQuote = latestQuote;
    }
}
