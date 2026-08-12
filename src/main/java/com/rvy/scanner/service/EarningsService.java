package com.rvy.scanner.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.rvy.scanner.client.EarningsClient;
import com.rvy.scanner.model.ExpirationGroup;
import com.rvy.scanner.model.OptionChain;

@Service
public class EarningsService {

    private final EarningsClient earningsClient;
    private final OptionCalculationService calculations;

    public EarningsService(EarningsClient earningsClient, OptionCalculationService calculations) {
        this.earningsClient = earningsClient;
        this.calculations = calculations;
    }

    public void apply(OptionChain chain) {
        if (chain == null || chain.getUnderlyingSymbol() == null) {
            return;
        }
        Optional<LocalDate> earnings = earningsClient.nextEarningsDate(chain.getUnderlyingSymbol());
        if (earnings.isEmpty()) {
            return;
        }
        LocalDate date = earnings.get();
        chain.setEarningsDate(date);
        LocalDate today = calculations.today();
        for (ExpirationGroup group : chain.getExpirations()) {
            boolean flagged = !date.isBefore(today) && !date.isAfter(group.getExpiration());
            group.setEarningsDate(date);
            group.setEarningsBeforeExpiration(flagged);
        }
    }
}
