package com.rvy.scanner.web;

import com.rvy.scanner.model.StrangleCandidate;

public class StrikeVisualization {

    private final double min;
    private final double max;

    public StrikeVisualization(StrangleCandidate candidate) {
        double put = candidate.getPutStrike();
        double call = candidate.getCallStrike();
        double spot = candidate.getUnderlyingPrice();
        double lower = candidate.getLowerBreakeven();
        double upper = candidate.getUpperBreakeven();
        double pad = Math.max(1.0, (call - put) * 0.08);
        this.min = Math.min(put, Math.min(lower, spot)) - pad;
        this.max = Math.max(call, Math.max(upper, spot)) + pad;
    }

    public double putX(StrangleCandidate candidate) {
        return x(candidate.getPutStrike());
    }

    public double callX(StrangleCandidate candidate) {
        return x(candidate.getCallStrike());
    }

    public double spotX(StrangleCandidate candidate) {
        return x(candidate.getUnderlyingPrice());
    }

    public double lowerX(StrangleCandidate candidate) {
        return x(candidate.getLowerBreakeven());
    }

    public double upperX(StrangleCandidate candidate) {
        return x(candidate.getUpperBreakeven());
    }

    private double x(double price) {
        double span = max - min;
        if (span <= 0) {
            return 50;
        }
        return ((price - min) / span) * 100.0;
    }
}
