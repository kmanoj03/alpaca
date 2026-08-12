package com.rvy.scanner.support;

import java.time.LocalDate;

import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.OptionGreeks;
import com.rvy.scanner.model.OptionQuote;
import com.rvy.scanner.model.OptionType;
import com.rvy.scanner.model.StrategyParameters;

public final class ContractFixtures {

    private ContractFixtures() {
    }

    public static StrategyParameters defaultParams() {
        StrategyParameters params = new StrategyParameters();
        params.setMinDelta(0.05);
        params.setMaxDelta(0.15);
        params.setMinTheta(0.015);
        params.setMinDte(7);
        params.setMaxDte(45);
        params.setMinPremium(0.20);
        params.setMaxSpread(0.20);
        params.setMinOpenInterest(500);
        params.setMinVolume(0);
        return params;
    }

    public static OptionContract call(
            String symbol,
            LocalDate expiration,
            double strike,
            double delta,
            double theta,
            double bid,
            double ask) {
        return contract(symbol, OptionType.CALL, expiration, strike, delta, theta, bid, ask, 1000, 800L);
    }

    public static OptionContract put(
            String symbol,
            LocalDate expiration,
            double strike,
            double delta,
            double theta,
            double bid,
            double ask) {
        return contract(symbol, OptionType.PUT, expiration, strike, delta, theta, bid, ask, 1000, 800L);
    }

    public static OptionContract contract(
            String symbol,
            OptionType type,
            LocalDate expiration,
            double strike,
            Double delta,
            Double theta,
            Double bid,
            Double ask,
            long volume,
            Long openInterest) {
        OptionContract contract = new OptionContract();
        contract.setSymbol(symbol);
        contract.setUnderlyingSymbol("SPY");
        contract.setType(type);
        contract.setExpiration(expiration);
        contract.setStrike(strike);
        contract.setVolume(volume);
        contract.setOpenInterest(openInterest);
        if (delta != null || theta != null) {
            OptionGreeks greeks = new OptionGreeks();
            greeks.setDelta(delta);
            greeks.setTheta(theta);
            contract.setGreeks(greeks);
        }
        if (bid != null || ask != null) {
            OptionQuote quote = new OptionQuote();
            quote.setBid(bid);
            quote.setAsk(ask);
            contract.setQuote(quote);
        }
        return contract;
    }
}
