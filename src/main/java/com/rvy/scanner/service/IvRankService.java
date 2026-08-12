package com.rvy.scanner.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rvy.scanner.client.AlpacaClient;
import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.StockBar;

@Service
public class IvRankService {

    private static final Logger log = LoggerFactory.getLogger(IvRankService.class);
    private static final int HV_WINDOW = 30;

    private final AlpacaClient alpacaClient;
    private final OptionCalculationService calculations;

    public IvRankService(AlpacaClient alpacaClient, OptionCalculationService calculations) {
        this.alpacaClient = alpacaClient;
        this.calculations = calculations;
    }

    public void apply(OptionChain chain) {
        if (chain == null || chain.getContracts() == null || chain.getContracts().isEmpty()) {
            return;
        }
        Double atmIv = atmIv(chain);
        chain.setAtmIv(atmIv);
        if (atmIv == null) {
            return;
        }
        try {
            LocalDate end = calculations.today();
            LocalDate start = end.minusYears(1).minusDays(HV_WINDOW + 5);
            List<StockBar> bars = alpacaClient.fetchDailyBars(chain.getUnderlyingSymbol(), start, end);
            chain.setIvVsHvPercentile(percentile(atmIv, historicalHv(bars)));
        } catch (RuntimeException ex) {
            log.info("IV vs HV percentile unavailable for {}: {}", chain.getUnderlyingSymbol(), ex.getMessage());
        }
    }

    Double atmIv(OptionChain chain) {
        OptionContract call = nearest(chain.getContracts(), chain.getUnderlyingPrice(), true);
        OptionContract put = nearest(chain.getContracts(), chain.getUnderlyingPrice(), false);
        if (call != null && call.getImpliedVolatility() != null) {
            return call.getImpliedVolatility();
        }
        if (put != null && put.getImpliedVolatility() != null) {
            return put.getImpliedVolatility();
        }
        return null;
    }

    List<Double> historicalHv(List<StockBar> bars) {
        if (bars == null || bars.size() < HV_WINDOW + 1) {
            return List.of();
        }
        List<StockBar> sorted = bars.stream().sorted(Comparator.comparing(StockBar::getDate)).toList();
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < sorted.size(); i++) {
            double prev = sorted.get(i - 1).getClose();
            double close = sorted.get(i).getClose();
            if (prev > 0 && close > 0) {
                returns.add(Math.log(close / prev));
            }
        }
        List<Double> hv = new ArrayList<>();
        for (int i = HV_WINDOW - 1; i < returns.size(); i++) {
            hv.add(annualizedStdev(returns.subList(i - HV_WINDOW + 1, i + 1)));
        }
        return hv;
    }

    Double percentile(double value, List<Double> distribution) {
        if (distribution == null || distribution.isEmpty()) {
            return null;
        }
        long below = distribution.stream().filter(sample -> sample <= value).count();
        return 100.0 * below / distribution.size();
    }

    private double annualizedStdev(List<Double> window) {
        double mean = window.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = 0;
        for (double value : window) {
            variance += Math.pow(value - mean, 2);
        }
        variance /= Math.max(1, window.size() - 1);
        return Math.sqrt(variance) * Math.sqrt(252);
    }

    private OptionContract nearest(List<OptionContract> contracts, double spot, boolean call) {
        return contracts.stream()
                .filter(contract -> call ? contract.isCall() : contract.isPut())
                .min(Comparator.comparingDouble(contract -> Math.abs(contract.getStrike() - spot)))
                .orElse(null);
    }
}
