package com.rvy.scanner.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class StrangleCandidate {

    private String id;
    private int rank;
    private double score;
    private String underlyingSymbol;
    private double underlyingPrice;
    private LocalDate expiration;
    private int dte;
    private boolean weekly;
    private OptionContract call;
    private OptionContract put;
    private double totalPremium;
    private double premiumPerContract;
    private double lowerBreakeven;
    private double upperBreakeven;
    private double callDistancePct;
    private double putDistancePct;
    private ExpectedMove expectedMove;
    private Double averageIv;
    private Map<String, Double> scoreFactors = new LinkedHashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

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

    public LocalDate getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDate expiration) {
        this.expiration = expiration;
    }

    public int getDte() {
        return dte;
    }

    public void setDte(int dte) {
        this.dte = dte;
    }

    public boolean isWeekly() {
        return weekly;
    }

    public void setWeekly(boolean weekly) {
        this.weekly = weekly;
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

    public double getCallStrike() {
        return call == null ? 0 : call.getStrike();
    }

    public double getPutStrike() {
        return put == null ? 0 : put.getStrike();
    }

    public Double getCallDelta() {
        return call == null ? null : call.getDelta();
    }

    public Double getPutDelta() {
        return put == null ? null : put.getDelta();
    }

    public Double getCallTheta() {
        return call == null ? null : call.getTheta();
    }

    public Double getPutTheta() {
        return put == null ? null : put.getTheta();
    }

    public double getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(double totalPremium) {
        this.totalPremium = totalPremium;
    }

    public double getPremiumPerContract() {
        return premiumPerContract;
    }

    public void setPremiumPerContract(double premiumPerContract) {
        this.premiumPerContract = premiumPerContract;
    }

    public double getLowerBreakeven() {
        return lowerBreakeven;
    }

    public void setLowerBreakeven(double lowerBreakeven) {
        this.lowerBreakeven = lowerBreakeven;
    }

    public double getUpperBreakeven() {
        return upperBreakeven;
    }

    public void setUpperBreakeven(double upperBreakeven) {
        this.upperBreakeven = upperBreakeven;
    }

    public double getCallDistancePct() {
        return callDistancePct;
    }

    public void setCallDistancePct(double callDistancePct) {
        this.callDistancePct = callDistancePct;
    }

    public double getPutDistancePct() {
        return putDistancePct;
    }

    public void setPutDistancePct(double putDistancePct) {
        this.putDistancePct = putDistancePct;
    }

    public ExpectedMove getExpectedMove() {
        return expectedMove;
    }

    public void setExpectedMove(ExpectedMove expectedMove) {
        this.expectedMove = expectedMove;
    }

    public Double getAverageIv() {
        return averageIv;
    }

    public void setAverageIv(Double averageIv) {
        this.averageIv = averageIv;
    }

    public Map<String, Double> getScoreFactors() {
        return scoreFactors;
    }

    public void setScoreFactors(Map<String, Double> scoreFactors) {
        this.scoreFactors = scoreFactors;
    }
}
