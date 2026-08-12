package com.rvy.scanner.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpirationGroup {

    private static final DateTimeFormatter HEADER = DateTimeFormatter.ofPattern("dd MMM yy", Locale.US);

    private LocalDate expiration;
    private int dte;
    private boolean weekly;
    private int contractSize = 100;
    private boolean expanded;
    private List<StrikeRow> strikes = new ArrayList<>();

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

    public int getContractSize() {
        return contractSize;
    }

    public void setContractSize(int contractSize) {
        this.contractSize = contractSize;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public List<StrikeRow> getStrikes() {
        return strikes;
    }

    public void setStrikes(List<StrikeRow> strikes) {
        this.strikes = strikes;
    }

    public String getHeaderLabel() {
        if (expiration == null) {
            return "";
        }
        String label = expiration.format(HEADER);
        if (weekly) {
            label += " (W)";
        }
        return label;
    }
}
