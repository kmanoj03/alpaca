package com.rvy.scanner.model;

import com.rvy.scanner.config.ScannerProperties;

public class StrategyParameters {

    private double minDelta;
    private double maxDelta;
    private double minTheta;
    private int minDte;
    private int maxDte;
    private double minPremium;
    private double maxSpread;
    private int minOpenInterest;
    private int minVolume;

    public static StrategyParameters fromDefaults(ScannerProperties.Defaults defaults) {
        StrategyParameters params = new StrategyParameters();
        params.setMinDelta(defaults.getMinDelta());
        params.setMaxDelta(defaults.getMaxDelta());
        params.setMinTheta(defaults.getMinTheta());
        params.setMinDte(defaults.getMinDte());
        params.setMaxDte(defaults.getMaxDte());
        params.setMinPremium(defaults.getMinPremium());
        params.setMaxSpread(defaults.getMaxSpread());
        params.setMinOpenInterest(defaults.getMinOpenInterest());
        params.setMinVolume(defaults.getMinVolume());
        return params;
    }

    public double getMinDelta() {
        return minDelta;
    }

    public void setMinDelta(double minDelta) {
        this.minDelta = minDelta;
    }

    public double getMaxDelta() {
        return maxDelta;
    }

    public void setMaxDelta(double maxDelta) {
        this.maxDelta = maxDelta;
    }

    public double getMinTheta() {
        return minTheta;
    }

    public void setMinTheta(double minTheta) {
        this.minTheta = minTheta;
    }

    public int getMinDte() {
        return minDte;
    }

    public void setMinDte(int minDte) {
        this.minDte = minDte;
    }

    public int getMaxDte() {
        return maxDte;
    }

    public void setMaxDte(int maxDte) {
        this.maxDte = maxDte;
    }

    public double getMinPremium() {
        return minPremium;
    }

    public void setMinPremium(double minPremium) {
        this.minPremium = minPremium;
    }

    public double getMaxSpread() {
        return maxSpread;
    }

    public void setMaxSpread(double maxSpread) {
        this.maxSpread = maxSpread;
    }

    public int getMinOpenInterest() {
        return minOpenInterest;
    }

    public void setMinOpenInterest(int minOpenInterest) {
        this.minOpenInterest = minOpenInterest;
    }

    public int getMinVolume() {
        return minVolume;
    }

    public void setMinVolume(int minVolume) {
        this.minVolume = minVolume;
    }
}
