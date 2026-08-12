package com.rvy.scanner.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionQuoteDto {

    private Double bp;
    private Double ap;
    private Integer bs;
    private Integer as;

    public Double getBp() {
        return bp;
    }

    public void setBp(Double bp) {
        this.bp = bp;
    }

    public Double getAp() {
        return ap;
    }

    public void setAp(Double ap) {
        this.ap = ap;
    }

    public Integer getBs() {
        return bs;
    }

    public void setBs(Integer bs) {
        this.bs = bs;
    }

    public Integer getAs() {
        return as;
    }

    public void setAs(Integer as) {
        this.as = as;
    }
}
