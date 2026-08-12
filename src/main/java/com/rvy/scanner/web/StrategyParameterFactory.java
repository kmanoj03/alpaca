package com.rvy.scanner.web;

import org.springframework.stereotype.Component;

import com.rvy.scanner.config.ScannerProperties;
import com.rvy.scanner.model.StrategyParameters;

@Component
public class StrategyParameterFactory {

    private final ScannerProperties scannerProperties;

    public StrategyParameterFactory(ScannerProperties scannerProperties) {
        this.scannerProperties = scannerProperties;
    }

    public StrategyParameters defaults() {
        return StrategyParameters.fromDefaults(scannerProperties.getDefaults());
    }

    public StrategyParameters from(
            Double minDelta,
            Double maxDelta,
            Double minTheta,
            Integer minDte,
            Integer maxDte,
            Double minPremium,
            Double maxSpread,
            Integer minOpenInterest,
            Integer minVolume) {
        StrategyParameters params = defaults();
        if (minDelta != null) {
            params.setMinDelta(minDelta);
        }
        if (maxDelta != null) {
            params.setMaxDelta(maxDelta);
        }
        if (minTheta != null) {
            params.setMinTheta(minTheta);
        }
        if (minDte != null) {
            params.setMinDte(minDte);
        }
        if (maxDte != null) {
            params.setMaxDte(maxDte);
        }
        if (minPremium != null) {
            params.setMinPremium(minPremium);
        }
        if (maxSpread != null) {
            params.setMaxSpread(maxSpread);
        }
        if (minOpenInterest != null) {
            params.setMinOpenInterest(minOpenInterest);
        }
        if (minVolume != null) {
            params.setMinVolume(minVolume);
        }
        return params;
    }
}
