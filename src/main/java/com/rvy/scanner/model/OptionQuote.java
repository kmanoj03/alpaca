package com.rvy.scanner.model;

public class OptionQuote {

    private Double bid;
    private Double ask;
    private Integer bidSize;
    private Integer askSize;

    public Double getBid() {
        return bid;
    }

    public void setBid(Double bid) {
        this.bid = bid;
    }

    public Double getAsk() {
        return ask;
    }

    public void setAsk(Double ask) {
        this.ask = ask;
    }

    public Integer getBidSize() {
        return bidSize;
    }

    public void setBidSize(Integer bidSize) {
        this.bidSize = bidSize;
    }

    public Integer getAskSize() {
        return askSize;
    }

    public void setAskSize(Integer askSize) {
        this.askSize = askSize;
    }

    public boolean hasBidAndAsk() {
        return bid != null && ask != null;
    }

    public boolean hasPositiveBidAndAsk() {
        return bid != null && ask != null && bid > 0 && ask > 0;
    }

    public Double mid() {
        if (!hasBidAndAsk()) {
            return null;
        }
        return (bid + ask) / 2.0;
    }

    public Double spread() {
        if (!hasBidAndAsk()) {
            return null;
        }
        return ask - bid;
    }
}
