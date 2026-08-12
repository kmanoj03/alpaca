package com.rvy.scanner.model;

import java.util.ArrayList;
import java.util.List;

public class OptionChain {

    private String underlyingSymbol;
    private double underlyingPrice;
    private List<ExpirationGroup> expirations = new ArrayList<>();
    private List<OptionContract> contracts = new ArrayList<>();

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
}
