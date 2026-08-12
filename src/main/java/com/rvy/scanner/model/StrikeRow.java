package com.rvy.scanner.model;

public class StrikeRow {

    private double strike;
    private OptionContract call;
    private OptionContract put;

    public StrikeRow() {
    }

    public StrikeRow(double strike) {
        this.strike = strike;
    }

    public double getStrike() {
        return strike;
    }

    public void setStrike(double strike) {
        this.strike = strike;
    }

    public OptionContract getCall() {
        return call;
    }

    public void setCall(OptionContract call) {
        this.call = call;
    }

    public OptionContract getPut() {
        return put;
    }

    public void setPut(OptionContract put) {
        this.put = put;
    }
}
