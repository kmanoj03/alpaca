package com.rvy.scanner.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionBarDto {

    private Long v;
    private Double c;
    private Double o;
    private Double h;
    private Double l;
    private String t;

    public Long getV() {
        return v;
    }

    public void setV(Long v) {
        this.v = v;
    }

    public Double getC() {
        return c;
    }

    public void setC(Double c) {
        this.c = c;
    }

    public Double getO() {
        return o;
    }

    public void setO(Double o) {
        this.o = o;
    }

    public Double getH() {
        return h;
    }

    public void setH(Double h) {
        this.h = h;
    }

    public Double getL() {
        return l;
    }

    public void setL(Double l) {
        this.l = l;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }
}
