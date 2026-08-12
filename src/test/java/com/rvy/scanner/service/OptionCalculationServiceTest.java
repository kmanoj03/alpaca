package com.rvy.scanner.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.support.ContractFixtures;

class OptionCalculationServiceTest {

    private OptionCalculationService calculations;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-09T16:00:00Z"), ZoneOffset.UTC);
        calculations = new OptionCalculationService(clock);
    }

    @Test
    void calculatesDteFromMarketToday() {
        assertThat(calculations.today()).isEqualTo(LocalDate.of(2026, 8, 9));
        assertThat(calculations.dte(LocalDate.of(2026, 8, 21))).isEqualTo(12);
        assertThat(calculations.dte(LocalDate.of(2026, 8, 9))).isEqualTo(0);
        assertThat(calculations.dte(LocalDate.of(2026, 8, 8))).isEqualTo(-1);
    }

    @Test
    void identifiesWeeklyVersusMonthlyExpiration() {
        assertThat(calculations.thirdFriday(2026, 8)).isEqualTo(LocalDate.of(2026, 8, 21));
        assertThat(calculations.isWeekly(LocalDate.of(2026, 8, 21))).isFalse();
        assertThat(calculations.isWeekly(LocalDate.of(2026, 8, 14))).isTrue();
        assertThat(calculations.isWeekly(LocalDate.of(2026, 8, 28))).isTrue();
    }

    @Test
    void identifiesOtmCallsAndPuts() {
        OptionContract call = ContractFixtures.call("C", LocalDate.of(2026, 8, 21), 680, 0.08, -0.018, 0.35, 0.40);
        OptionContract put = ContractFixtures.put("P", LocalDate.of(2026, 8, 21), 625, -0.07, -0.016, 0.28, 0.32);
        OptionContract itmCall = ContractFixtures.call("ITM", LocalDate.of(2026, 8, 21), 640, 0.6, -0.05, 12, 12.2);

        assertThat(calculations.isOtm(call, 650)).isTrue();
        assertThat(calculations.isOtm(put, 650)).isTrue();
        assertThat(calculations.isOtm(itmCall, 650)).isFalse();
        assertThat(calculations.isOtmPut(655, 650)).isFalse();
    }

    @Test
    void calculatesPremiumAndBreakevensFromAssignmentFixture() {
        double total = calculations.totalPremium(0.375, 0.300);
        assertThat(total).isEqualTo(0.675);
        assertThat(calculations.premiumPerContract(total, 100)).isEqualTo(67.5);
        assertThat(calculations.lowerBreakeven(625, total)).isEqualTo(624.325);
        assertThat(calculations.upperBreakeven(680, total)).isEqualTo(680.675);
        assertThat(calculations.callDistancePct(680, 650)).isCloseTo(0.046153, org.assertj.core.data.Offset.offset(0.000001));
        assertThat(calculations.putDistancePct(625, 650)).isCloseTo(0.038461, org.assertj.core.data.Offset.offset(0.000001));
    }

    @Test
    void usesAbsoluteThetaAndDelta() {
        OptionContract put = ContractFixtures.put("P", LocalDate.of(2026, 8, 21), 625, -0.07, -0.016, 0.28, 0.32);
        assertThat(calculations.absDelta(put)).isEqualTo(0.07);
        assertThat(calculations.absTheta(put)).isEqualTo(0.016);
    }
}
