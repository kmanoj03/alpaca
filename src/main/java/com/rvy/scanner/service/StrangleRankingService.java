package com.rvy.scanner.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rvy.scanner.config.ScannerProperties;
import com.rvy.scanner.model.ExpectedMove;
import com.rvy.scanner.model.StrangleCandidate;
import com.rvy.scanner.model.StrategyParameters;

@Service
public class StrangleRankingService {

    private final OptionCalculationService calculations;
    private final ScannerProperties scannerProperties;

    public StrangleRankingService(OptionCalculationService calculations, ScannerProperties scannerProperties) {
        this.calculations = calculations;
        this.scannerProperties = scannerProperties;
    }

    public List<StrangleCandidate> rank(List<StrangleCandidate> candidates, StrategyParameters params) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        Map<String, double[]> raw = new HashMap<>();
        raw.put("premium", candidates.stream().mapToDouble(StrangleCandidate::getTotalPremium).toArray());
        raw.put("liquidity", candidates.stream().mapToDouble(this::liquidityRaw).toArray());
        raw.put("theta", candidates.stream().mapToDouble(this::thetaRaw).toArray());
        raw.put("delta", candidates.stream().mapToDouble(this::deltaRaw).toArray());
        raw.put("distance", candidates.stream().mapToDouble(this::distanceRaw).toArray());
        raw.put("dte", candidates.stream().mapToDouble(c -> dteRaw(c, params)).toArray());
        raw.put("iv", candidates.stream().mapToDouble(this::ivRaw).toArray());
        raw.put("expectedMove", candidates.stream().mapToDouble(this::expectedMoveRaw).toArray());

        Map<String, Double> mins = new HashMap<>();
        Map<String, Double> maxs = new HashMap<>();
        for (Map.Entry<String, double[]> entry : raw.entrySet()) {
            DoubleSummaryStatistics stats = java.util.Arrays.stream(entry.getValue()).summaryStatistics();
            mins.put(entry.getKey(), stats.getMin());
            maxs.put(entry.getKey(), stats.getMax());
        }

        ScannerProperties.Weights weights = scannerProperties.getRanking().getWeights();
        for (int i = 0; i < candidates.size(); i++) {
            StrangleCandidate candidate = candidates.get(i);
            Map<String, Double> factors = new LinkedHashMap<>();
            factors.put("premium", normalize(raw.get("premium")[i], mins.get("premium"), maxs.get("premium")));
            factors.put("liquidity", normalize(raw.get("liquidity")[i], mins.get("liquidity"), maxs.get("liquidity")));
            factors.put("theta", normalize(raw.get("theta")[i], mins.get("theta"), maxs.get("theta")));
            factors.put("delta", normalize(raw.get("delta")[i], mins.get("delta"), maxs.get("delta")));
            factors.put("distance", normalize(raw.get("distance")[i], mins.get("distance"), maxs.get("distance")));
            factors.put("dte", normalize(raw.get("dte")[i], mins.get("dte"), maxs.get("dte")));
            factors.put("iv", normalize(raw.get("iv")[i], mins.get("iv"), maxs.get("iv")));
            factors.put("expectedMove", normalize(raw.get("expectedMove")[i], mins.get("expectedMove"), maxs.get("expectedMove")));
            candidate.setScoreFactors(factors);
            candidate.setScore(100.0 * (
                    weights.getPremium() * factors.get("premium")
                            + weights.getLiquidity() * factors.get("liquidity")
                            + weights.getTheta() * factors.get("theta")
                            + weights.getDelta() * factors.get("delta")
                            + weights.getDistance() * factors.get("distance")
                            + weights.getDte() * factors.get("dte")
                            + weights.getIv() * factors.get("iv")
                            + weights.getExpectedMove() * factors.get("expectedMove")));
        }

        List<StrangleCandidate> ranked = new ArrayList<>(candidates);
        ranked.sort(Comparator.comparingDouble(StrangleCandidate::getScore).reversed()
                .thenComparing(Comparator.comparingDouble(StrangleCandidate::getTotalPremium).reversed()));
        int topN = scannerProperties.getRanking().getTopN();
        if (ranked.size() > topN) {
            ranked = new ArrayList<>(ranked.subList(0, topN));
        }
        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).setRank(i + 1);
        }
        return ranked;
    }

    double liquidityRaw(StrangleCandidate candidate) {
        double callSpread = orZero(candidate.getCall().getSpread());
        double putSpread = orZero(candidate.getPut().getSpread());
        double avgSpread = (callSpread + putSpread) / 2.0;
        double avgVolume = (candidate.getCall().getVolume() + candidate.getPut().getVolume()) / 2.0;
        double callOi = candidate.getCall().getOpenInterest() == null ? 0 : candidate.getCall().getOpenInterest();
        double putOi = candidate.getPut().getOpenInterest() == null ? 0 : candidate.getPut().getOpenInterest();
        double avgOi = (callOi + putOi) / 2.0;
        return (1.0 / (1.0 + avgSpread)) * Math.log1p(avgVolume) * Math.log1p(avgOi);
    }

    double thetaRaw(StrangleCandidate candidate) {
        return (calculations.absTheta(candidate.getCall()) + calculations.absTheta(candidate.getPut())) / 2.0;
    }

    /**
     * Higher is further OTM (lower |delta|). This is only one weighted input, not the rank itself.
     */
    double deltaRaw(StrangleCandidate candidate) {
        return 1.0 - ((calculations.absDelta(candidate.getCall()) + calculations.absDelta(candidate.getPut())) / 2.0);
    }

    double distanceRaw(StrangleCandidate candidate) {
        double avg = (candidate.getCallDistancePct() + candidate.getPutDistancePct()) / 2.0;
        if (avg < 0.05) {
            return avg / 0.05 * 0.5;
        }
        if (avg <= 0.20) {
            return 0.5 + (avg - 0.05) / 0.15 * 0.5;
        }
        return Math.max(0.0, 1.0 - (avg - 0.20) / 0.20);
    }

    double dteRaw(StrangleCandidate candidate, StrategyParameters params) {
        double mid = (params.getMinDte() + params.getMaxDte()) / 2.0;
        double halfRange = Math.max(1.0, (params.getMaxDte() - params.getMinDte()) / 2.0);
        return 1.0 - Math.min(1.0, Math.abs(candidate.getDte() - mid) / halfRange);
    }

    double ivRaw(StrangleCandidate candidate) {
        return candidate.getAverageIv() == null ? 0.0 : candidate.getAverageIv();
    }

    double expectedMoveRaw(StrangleCandidate candidate) {
        ExpectedMove move = candidate.getExpectedMove();
        if (move == null || move.getUsed() == null || move.getUsed() <= 0) {
            return 0.0;
        }
        double lowerBuffer = (candidate.getUnderlyingPrice() - move.getUsed()) - candidate.getPutStrike();
        double upperBuffer = candidate.getCallStrike() - (candidate.getUnderlyingPrice() + move.getUsed());
        return (clamp(lowerBuffer / move.getUsed()) + clamp(upperBuffer / move.getUsed())) / 2.0;
    }

    private static double normalize(double value, double min, double max) {
        if (Double.compare(max, min) == 0) {
            return 0.5;
        }
        return (value - min) / (max - min);
    }

    private static double orZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private static double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 1.5);
    }
}
