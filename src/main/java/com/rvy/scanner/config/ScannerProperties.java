package com.rvy.scanner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scanner")
public class ScannerProperties {

    private Defaults defaults = new Defaults();
    private Ranking ranking = new Ranking();

    public Defaults getDefaults() {
        return defaults;
    }

    public void setDefaults(Defaults defaults) {
        this.defaults = defaults;
    }

    public Ranking getRanking() {
        return ranking;
    }

    public void setRanking(Ranking ranking) {
        this.ranking = ranking;
    }

    public static class Defaults {
        private double minDelta = 0.05;
        private double maxDelta = 0.15;
        private double minTheta = 0.015;
        private int minDte = 7;
        private int maxDte = 45;
        private double minPremium = 0.20;
        private double maxSpread = 0.20;
        private int minOpenInterest = 500;
        private int minVolume = 0;

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

    public static class Ranking {
        private int topN = 25;
        private Weights weights = new Weights();

        public int getTopN() {
            return topN;
        }

        public void setTopN(int topN) {
            this.topN = topN;
        }

        public Weights getWeights() {
            return weights;
        }

        public void setWeights(Weights weights) {
            this.weights = weights;
        }
    }

    public static class Weights {
        private double premium = 0.20;
        private double liquidity = 0.18;
        private double theta = 0.12;
        private double delta = 0.12;
        private double distance = 0.12;
        private double dte = 0.08;
        private double iv = 0.10;
        private double expectedMove = 0.08;

        public double getPremium() {
            return premium;
        }

        public void setPremium(double premium) {
            this.premium = premium;
        }

        public double getLiquidity() {
            return liquidity;
        }

        public void setLiquidity(double liquidity) {
            this.liquidity = liquidity;
        }

        public double getTheta() {
            return theta;
        }

        public void setTheta(double theta) {
            this.theta = theta;
        }

        public double getDelta() {
            return delta;
        }

        public void setDelta(double delta) {
            this.delta = delta;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public double getDte() {
            return dte;
        }

        public void setDte(double dte) {
            this.dte = dte;
        }

        public double getIv() {
            return iv;
        }

        public void setIv(double iv) {
            this.iv = iv;
        }

        public double getExpectedMove() {
            return expectedMove;
        }

        public void setExpectedMove(double expectedMove) {
            this.expectedMove = expectedMove;
        }
    }
}
