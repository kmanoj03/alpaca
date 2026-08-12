package com.rvy.scanner.service;

import java.time.LocalDate;
import java.util.List;

import com.rvy.scanner.model.StockBar;

public class PnlCalculator {

    public Evaluation evaluate(
            LocalDate expiration,
            double putStrike,
            double callStrike,
            double totalPremium,
            int contractSize,
            double entryPrice,
            double currentPrice,
            LocalDate today,
            List<StockBar> barsAfterEntry) {
        boolean expired = !expiration.isAfter(today);
        boolean stayedBetween = currentPrice >= putStrike && currentPrice <= callStrike;
        double callIntrinsic = Math.max(0, currentPrice - callStrike);
        double putIntrinsic = Math.max(0, putStrike - currentPrice);
        double theoreticalPl = (totalPremium - callIntrinsic - putIntrinsic) * contractSize;
        double maxAdverse = Math.abs(currentPrice - entryPrice);
        if (barsAfterEntry != null) {
            for (StockBar bar : barsAfterEntry) {
                maxAdverse = Math.max(maxAdverse, Math.abs(bar.getHigh() - entryPrice));
                maxAdverse = Math.max(maxAdverse, Math.abs(entryPrice - bar.getLow()));
            }
        }
        return new Evaluation(expired, stayedBetween, theoreticalPl, maxAdverse);
    }

    public record Evaluation(boolean expired, boolean stayedBetweenStrikes, double theoreticalPl, double maxAdverseMove) {
    }
}
