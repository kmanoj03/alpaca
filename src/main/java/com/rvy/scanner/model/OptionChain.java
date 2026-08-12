package com.rvy.scanner.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OptionChain {

    private String underlyingSymbol;
    private double underlyingPrice;
    private List<ExpirationGroup> expirations = new ArrayList<>();
    private List<OptionContract> contracts = new ArrayList<>();
    private Double atmIv;
    private Double ivVsHvPercentile;
    private LocalDate earningsDate;

    public String getUnderlyingSymbol() {
        return underlyingSymbol;
    }

    public void setUnderlyingSymbol(String underlyingSymbol) {
        this.underlyingSymbol = underlyingSymbol;
    }

    public double getUnderlyingPrice() {
        return underlyingPrice;
    }

    public void setUnderlyingPrice(double underlyingPrice) {
        this.underlyingPrice = underlyingPrice;
    }

    public List<ExpirationGroup> getExpirations() {
        return expirations;
    }

    public void setExpirations(List<ExpirationGroup> expirations) {
        this.expirations = expirations;
    }

    public List<OptionContract> getContracts() {
        return contracts;
    }

    public void setContracts(List<OptionContract> contracts) {
        this.contracts = contracts;
    }

    public Double getAtmIv() {
        return atmIv;
    }

    public void setAtmIv(Double atmIv) {
        this.atmIv = atmIv;
    }

    public Double getIvVsHvPercentile() {
        return ivVsHvPercentile;
    }

    public void setIvVsHvPercentile(Double ivVsHvPercentile) {
        this.ivVsHvPercentile = ivVsHvPercentile;
    }

    public LocalDate getEarningsDate() {
        return earningsDate;
    }

    public void setEarningsDate(LocalDate earningsDate) {
        this.earningsDate = earningsDate;
    }
}
