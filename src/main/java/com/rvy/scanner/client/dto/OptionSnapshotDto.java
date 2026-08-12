package com.rvy.scanner.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionSnapshotDto {

    private OptionGreeksDto greeks;
    private Double impliedVolatility;
    private OptionQuoteDto latestQuote;
    private OptionTradeDto latestTrade;
    private OptionBarDto dailyBar;

    public OptionGreeksDto getGreeks() {
        return greeks;
    }

    public void setGreeks(OptionGreeksDto greeks) {
        this.greeks = greeks;
    }

    public Double getImpliedVolatility() {
        return impliedVolatility;
    }

    public void setImpliedVolatility(Double impliedVolatility) {
        this.impliedVolatility = impliedVolatility;
    }

    public OptionQuoteDto getLatestQuote() {
        return latestQuote;
    }

    public void setLatestQuote(OptionQuoteDto latestQuote) {
        this.latestQuote = latestQuote;
    }

    public OptionTradeDto getLatestTrade() {
        return latestTrade;
    }

    public void setLatestTrade(OptionTradeDto latestTrade) {
        this.latestTrade = latestTrade;
    }

    public OptionBarDto getDailyBar() {
        return dailyBar;
    }

    public void setDailyBar(OptionBarDto dailyBar) {
        this.dailyBar = dailyBar;
    }
}
