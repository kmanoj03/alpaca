package com.rvy.scanner.model;

public class OptionGreeks {

    private Double delta;
    private Double theta;
    private Double gamma;
    private Double vega;
    private Double rho;

    public Double getDelta() {
        return delta;
    }

    public void setDelta(Double delta) {
        this.delta = delta;
    }

    public Double getTheta() {
        return theta;
    }

    public void setTheta(Double theta) {
        this.theta = theta;
    }

    public Double getGamma() {
        return gamma;
    }

    public void setGamma(Double gamma) {
        this.gamma = gamma;
    }

    public Double getVega() {
        return vega;
    }

    public void setVega(Double vega) {
        this.vega = vega;
    }

    public Double getRho() {
        return rho;
    }

    public void setRho(Double rho) {
        this.rho = rho;
    }

    public boolean hasDeltaAndTheta() {
        return delta != null && theta != null;
    }
}
