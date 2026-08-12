package com.rvy.scanner.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.OptionQuote;
import com.rvy.scanner.model.StrategyParameters;

@Service
public class OptionFilterService {

    private final OptionCalculationService calculations;

    public OptionFilterService(OptionCalculationService calculations) {
        this.calculations = calculations;
    }

    public List<OptionContract> filterForScanner(
            List<OptionContract> contracts,
            double underlyingPrice,
            StrategyParameters params) {
        List<OptionContract> filtered = new ArrayList<>();
        if (contracts == null) {
            return filtered;
        }
        for (OptionContract contract : contracts) {
            if (passesScannerFilters(contract, underlyingPrice, params)) {
                filtered.add(contract);
            }
        }
        return filtered;
    }

    public List<OptionContract> calls(List<OptionContract> contracts) {
        return contracts.stream().filter(OptionContract::isCall).toList();
    }

    public List<OptionContract> puts(List<OptionContract> contracts) {
        return contracts.stream().filter(OptionContract::isPut).toList();
    }

    public boolean passesScannerFilters(
            OptionContract contract,
            double underlyingPrice,
            StrategyParameters params) {
        if (contract == null || contract.getExpiration() == null) {
            return false;
        }

        int dte = calculations.dte(contract.getExpiration());
        if (dte <= 0) {
            return false;
        }
        if (dte < params.getMinDte() || dte > params.getMaxDte()) {
            return false;
        }
        if (!calculations.isOtm(contract, underlyingPrice)) {
            return false;
        }
        if (contract.getGreeks() == null || !contract.getGreeks().hasDeltaAndTheta()) {
            return false;
        }
        if (contract.isCall() && contract.getDelta() <= 0) {
            return false;
        }
        if (contract.isPut() && contract.getDelta() >= 0) {
            return false;
        }

        double absDelta = calculations.absDelta(contract);
        if (absDelta < params.getMinDelta() || absDelta > params.getMaxDelta()) {
            return false;
        }
        if (calculations.absTheta(contract) < params.getMinTheta()) {
            return false;
        }

        OptionQuote quote = contract.getQuote();
        if (quote == null || !quote.hasPositiveBidAndAsk()) {
            return false;
        }
        Double mid = quote.mid();
        Double spread = quote.spread();
        if (mid == null || mid < params.getMinPremium()) {
            return false;
        }
        if (spread == null || spread > params.getMaxSpread()) {
            return false;
        }
        if (contract.getVolume() < params.getMinVolume()) {
            return false;
        }
        long openInterest = contract.getOpenInterest() == null ? 0L : contract.getOpenInterest();
        return openInterest >= params.getMinOpenInterest();
    }
}
