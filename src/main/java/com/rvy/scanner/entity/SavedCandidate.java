package com.rvy.scanner.entity;

import java.time.Instant;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "saved_candidates")
public class SavedCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id")
    private SavedScan scan;

    private String candidateKey;
    private LocalDate expiration;
    private int dte;
    private double putStrike;
    private double callStrike;
    private double totalPremium;
    private double premiumPerContract;
    private double lowerBreakeven;
    private double upperBreakeven;
    private double score;
    private boolean earningsBeforeExpiration;

    private Double latestUnderlying;
    private Boolean stayedBetweenStrikes;
    private Double theoreticalPl;
    private Double maxAdverseMove;
    private Boolean expired;
    private Instant evaluatedAt;

    public Long getId() {
        return id;
    }

    public SavedScan getScan() {
        return scan;
    }

    public void setScan(SavedScan scan) {
        this.scan = scan;
    }

    public String getCandidateKey() {
        return candidateKey;
    }

    public void setCandidateKey(String candidateKey) {
        this.candidateKey = candidateKey;
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

    public double getPutStrike() {
        return putStrike;
    }

    public void setPutStrike(double putStrike) {
        this.putStrike = putStrike;
    }

    public double getCallStrike() {
        return callStrike;
    }

    public void setCallStrike(double callStrike) {
        this.callStrike = callStrike;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public boolean isEarningsBeforeExpiration() {
        return earningsBeforeExpiration;
    }

    public void setEarningsBeforeExpiration(boolean earningsBeforeExpiration) {
        this.earningsBeforeExpiration = earningsBeforeExpiration;
    }

    public Double getLatestUnderlying() {
        return latestUnderlying;
    }

    public void setLatestUnderlying(Double latestUnderlying) {
        this.latestUnderlying = latestUnderlying;
    }

    public Boolean getStayedBetweenStrikes() {
        return stayedBetweenStrikes;
    }

    public void setStayedBetweenStrikes(Boolean stayedBetweenStrikes) {
        this.stayedBetweenStrikes = stayedBetweenStrikes;
    }

    public Double getTheoreticalPl() {
        return theoreticalPl;
    }

    public void setTheoreticalPl(Double theoreticalPl) {
        this.theoreticalPl = theoreticalPl;
    }

    public Double getMaxAdverseMove() {
        return maxAdverseMove;
    }

    public void setMaxAdverseMove(Double maxAdverseMove) {
        this.maxAdverseMove = maxAdverseMove;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Instant getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Instant evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}
