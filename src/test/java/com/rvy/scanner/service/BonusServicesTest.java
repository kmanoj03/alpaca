package com.rvy.scanner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rvy.scanner.client.AlpacaClient;
import com.rvy.scanner.client.EarningsClient;
import com.rvy.scanner.model.ExpirationGroup;
import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.StockBar;

@ExtendWith(MockitoExtension.class)
class BonusServicesTest {

    @Mock
    private EarningsClient earningsClient;

    @Mock
    private AlpacaClient alpacaClient;

    @Test
    void flagsExpirationsAfterEarningsDate() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-09T16:00:00Z"), ZoneOffset.UTC);
        EarningsService service = new EarningsService(earningsClient, new OptionCalculationService(clock));
        when(earningsClient.nextEarningsDate("SPY")).thenReturn(Optional.of(LocalDate.of(2026, 8, 18)));

        OptionChain chain = new OptionChain();
        chain.setUnderlyingSymbol("SPY");
        ExpirationGroup before = new ExpirationGroup();
        before.setExpiration(LocalDate.of(2026, 8, 14));
        ExpirationGroup after = new ExpirationGroup();
        after.setExpiration(LocalDate.of(2026, 8, 21));
        chain.setExpirations(List.of(before, after));

        service.apply(chain);

        assertThat(before.isEarningsBeforeExpiration()).isFalse();
        assertThat(after.isEarningsBeforeExpiration()).isTrue();
        assertThat(chain.getEarningsDate()).isEqualTo(LocalDate.of(2026, 8, 18));
    }

    @Test
    void ivVsHvPercentileRanksCurrentIvAgainstHvHistory() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-09T16:00:00Z"), ZoneOffset.UTC);
        IvRankService service = new IvRankService(alpacaClient, new OptionCalculationService(clock));
        List<StockBar> bars = new ArrayList<>();
        double price = 100;
        for (int i = 0; i < 80; i++) {
            StockBar bar = new StockBar();
            bar.setDate(LocalDate.of(2026, 1, 1).plusDays(i));
            price *= 1.001;
            bar.setClose(price);
            bar.setHigh(price);
            bar.setLow(price);
            bars.add(bar);
        }
        List<Double> hv = service.historicalHv(bars);
        assertThat(hv).isNotEmpty();
        Double percentile = service.percentile(hv.get(hv.size() - 1), hv);
        assertThat(percentile).isBetween(0.0, 100.0);
    }

    @Test
    void pnlKeepsPremiumWhenSpotStaysBetweenStrikes() {
        PnlCalculator calculator = new PnlCalculator();
        PnlCalculator.Evaluation evaluation = calculator.evaluate(
                LocalDate.of(2026, 8, 21),
                625,
                680,
                0.675,
                100,
                650,
                652,
                LocalDate.of(2026, 8, 21),
                List.of());
        assertThat(evaluation.expired()).isTrue();
        assertThat(evaluation.stayedBetweenStrikes()).isTrue();
        assertThat(evaluation.theoreticalPl()).isEqualTo(67.5);
    }

    @Test
    void pnlSubtractsIntrinsicWhenCallFinishesItm() {
        PnlCalculator calculator = new PnlCalculator();
        PnlCalculator.Evaluation evaluation = calculator.evaluate(
                LocalDate.of(2026, 8, 21),
                625,
                680,
                0.675,
                100,
                650,
                690,
                LocalDate.of(2026, 8, 22),
                List.of());
        assertThat(evaluation.stayedBetweenStrikes()).isFalse();
        assertThat(evaluation.theoreticalPl()).isEqualTo((0.675 - 10.0) * 100);
    }
}
