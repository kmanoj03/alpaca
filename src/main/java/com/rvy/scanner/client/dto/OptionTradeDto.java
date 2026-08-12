package com.rvy.scanner.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionTradeDto {

    private Double p;

    public Double getP() {
        return p;
    }

    public void setP(Double p) {
        this.p = p;
    }
}
