package com.rvy.scanner.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.OptionType;
import com.rvy.scanner.model.StrategyParameters;
import com.rvy.scanner.support.ContractFixtures;

class OptionFilterServiceTest {

    private static final LocalDate EXPIRY = LocalDate.of(2026, 8, 21);
    private static final double SPOT = 650.0;

    private OptionFilterService filters;
    private StrategyParameters params;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-09T16:00:00Z"), ZoneOffset.UTC);
        filters = new OptionFilterService(new OptionCalculationService(clock));
        params = ContractFixtures.defaultParams();
    }

    @Test
    void splitsCallsAndPuts() {
        OptionContract call = ContractFixtures.call("C", EXPIRY, 680, 0.08, -0.018, 0.35, 0.40);
        OptionContract put = ContractFixtures.put("P", EXPIRY, 625, -0.07, -0.016, 0.28, 0.32);
        List<OptionContract> contracts = List.of(call, put);

        assertThat(filters.calls(contracts)).containsExactly(call);
        assertThat(filters.puts(contracts)).containsExactly(put);
    }

    @Test
    void keepsDeepOtmContractsInsideConfiguredBands() {
        OptionContract call = ContractFixtures.call("C", EXPIRY, 680, 0.08, -0.018, 0.35, 0.40);
        OptionContract put = ContractFixtures.put("P", EXPIRY, 625, -0.07, -0.016, 0.28, 0.32);

        assertThat(filters.passesScannerFilters(call, SPOT, params)).isTrue();
        assertThat(filters.passesScannerFilters(put, SPOT, params)).isTrue();
    }

    @Test
    void excludesItmContractsFromScanner() {
        OptionContract itmCall = ContractFixtures.call("C", EXPIRY, 640, 0.08, -0.018, 0.35, 0.40);
        OptionContract itmPut = ContractFixtures.put("P", EXPIRY, 660, -0.07, -0.016, 0.28, 0.32);

        assertThat(filters.passesScannerFilters(itmCall, SPOT, params)).isFalse();
        assertThat(filters.passesScannerFilters(itmPut, SPOT, params)).isFalse();
    }

    @Test
    void filtersByAbsoluteDeltaRange() {
        OptionContract tooLow = ContractFixtures.call("C", EXPIRY, 720, 0.02, -0.018, 0.35, 0.40);
        OptionContract tooHigh = ContractFixtures.call("C", EXPIRY, 660, 0.25, -0.018, 0.35, 0.40);
        OptionContract putOutside = ContractFixtures.put("P", EXPIRY, 600, -0.20, -0.016, 0.28, 0.32);

        assertThat(filters.passesScannerFilters(tooLow, SPOT, params)).isFalse();
        assertThat(filters.passesScannerFilters(tooHigh, SPOT, params)).isFalse();
        assertThat(filters.passesScannerFilters(putOutside, SPOT, params)).isFalse();
    }

    @Test
    void filtersByAbsoluteTheta() {
        OptionContract lowTheta = ContractFixtures.call("C", EXPIRY, 680, 0.08, -0.005, 0.35, 0.40);
        assertThat(filters.passesScannerFilters(lowTheta, SPOT, params)).isFalse();
    }

    @Test
    void excludesExpiredAndZeroDte() {
        OptionContract zeroDte = ContractFixtures.call("C", LocalDate.of(2026, 8, 9), 680, 0.08, -0.018, 0.35, 0.40);
        OptionContract expired = ContractFixtures.call("C", LocalDate.of(2026, 8, 1), 680, 0.08, -0.018, 0.35, 0.40);
        assertThat(filters.passesScannerFilters(zeroDte, SPOT, params)).isFalse();
        assertThat(filters.passesScannerFilters(expired, SPOT, params)).isFalse();
    }

    @Test
    void excludesMissingGreeksZeroBidAndWideSpreads() {
        OptionContract missingGreeks = ContractFixtures.contract(
                "C", OptionType.CALL, EXPIRY, 680, null, null, 0.35, 0.40, 1000, 800L);
        OptionContract zeroBid = ContractFixtures.call("C", EXPIRY, 680, 0.08, -0.018, 0.0, 0.40);
        OptionContract wide = ContractFixtures.call("C", EXPIRY, 680, 0.08, -0.018, 0.10, 0.50);

        assertThat(filters.passesScannerFilters(missingGreeks, SPOT, params)).isFalse();
        assertThat(filters.passesScannerFilters(zeroBid, SPOT, params)).isFalse();
        assertThat(filters.passesScannerFilters(wide, SPOT, params)).isFalse();
    }

    @Test
    void excludesLowOpenInterest() {
        OptionContract lowOi = ContractFixtures.contract(
                "C", OptionType.CALL, EXPIRY, 680, 0.08, -0.018, 0.35, 0.40, 1000, 10L);
        assertThat(filters.passesScannerFilters(lowOi, SPOT, params)).isFalse();
    }
}
