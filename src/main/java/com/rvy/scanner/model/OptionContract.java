package com.rvy.scanner.model;

import java.time.LocalDate;

public class OptionContract {

    private String symbol;
    private String underlyingSymbol;
    private OptionType type;
    private double strike;
    private LocalDate expiration;
    private int size = 100;
    private OptionQuote quote;
    private OptionGreeks greeks;
    private Double impliedVolatility;
    private Double latestTradePrice;
    private long volume;
    private Long openInterest;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getUnderlyingSymbol() {
        return underlyingSymbol;
    }

    public void setUnderlyingSymbol(String underlyingSymbol) {
        this.underlyingSymbol = underlyingSymbol;
    }

    public OptionType getType() {
        return type;
    }

    public void setType(OptionType type) {
        this.type = type;
    }

    public boolean isCall() {
        return type == OptionType.CALL;
    }

    public boolean isPut() {
        return type == OptionType.PUT;
    }

    public double getStrike() {
        return strike;
    }

    public void setStrike(double strike) {
        this.strike = strike;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDate expiration) {
        this.expiration = expiration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public OptionQuote getQuote() {
        return quote;
    }

    public void setQuote(OptionQuote quote) {
        this.quote = quote;
    }

    public OptionGreeks getGreeks() {
        return greeks;
    }

    public void setGreeks(OptionGreeks greeks) {
        this.greeks = greeks;
    }

    public Double getImpliedVolatility() {
        return impliedVolatility;
    }

    public void setImpliedVolatility(Double impliedVolatility) {
        this.impliedVolatility = impliedVolatility;
    }

    public Double getLatestTradePrice() {
        return latestTradePrice;
    }

    public void setLatestTradePrice(Double latestTradePrice) {
        this.latestTradePrice = latestTradePrice;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public Long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(Long openInterest) {
        this.openInterest = openInterest;
    }

    public Double getDelta() {
        return greeks == null ? null : greeks.getDelta();
    }

    public Double getTheta() {
        return greeks == null ? null : greeks.getTheta();
    }

    public Double getBid() {
        return quote == null ? null : quote.getBid();
    }

    public Double getAsk() {
        return quote == null ? null : quote.getAsk();
    }

    public Double getMid() {
        return quote == null ? null : quote.mid();
    }

    public Double getSpread() {
        return quote == null ? null : quote.spread();
    }
}
