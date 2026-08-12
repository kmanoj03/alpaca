package com.rvy.scanner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rvy.scanner.config.ScannerProperties;
import com.rvy.scanner.model.ExpectedMove;
import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.StrangleCandidate;
import com.rvy.scanner.model.StrategyParameters;
import com.rvy.scanner.support.ContractFixtures;

class StrangleServiceTest {

    private static final LocalDate EXPIRY = LocalDate.of(2026, 8, 21);

    private OptionCalculationService calculations;
    private StrangleService strangleService;
    private StrangleRankingService rankingService;
    private StrategyParameters params;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-09T16:00:00Z"), ZoneOffset.UTC);
        calculations = new OptionCalculationService(clock);
        OptionFilterService filters = new OptionFilterService(calculations);
        rankingService = new StrangleRankingService(calculations, new ScannerProperties());
        strangleService = new StrangleService(filters, calculations, new ExpectedMoveService(), rankingService);
        params = ContractFixtures.defaultParams();
    }

    @Test
    void pairsCallAndPutAndMatchesAssignmentPremiumAndBreakevens() {
        OptionContract call = ContractFixtures.call("SPY260821C00680000", EXPIRY, 680, 0.08, -0.018, 0.35, 0.40);
        OptionContract put = ContractFixtures.put("SPY260821P00625000", EXPIRY, 625, -0.07, -0.016, 0.28, 0.32);
        OptionChain chain = chainOf(650, call, put);

        StrangleCandidate candidate = strangleService.toCandidate(chain, call, put, new ExpectedMove(), 12);

        assertThat(candidate.getTotalPremium()).isEqualTo(0.675);
        assertThat(candidate.getPremiumPerContract()).isEqualTo(67.5);
        assertThat(candidate.getLowerBreakeven()).isEqualTo(624.325);
        assertThat(candidate.getUpperBreakeven()).isEqualTo(680.675);
        assertThat(candidate.getCallDistancePct()).isCloseTo((680 - 650) / 650.0, within(1e-9));
        assertThat(candidate.getPutDistancePct()).isCloseTo((650 - 625) / 650.0, within(1e-9));
        assertThat(candidate.getDte()).isEqualTo(12);
    }

    @Test
    void scanPairsFilteredCallsAndPutsPerExpiration() {
        OptionContract call = ContractFixtures.call("SPY260821C00680000", EXPIRY, 680, 0.08, -0.018, 0.35, 0.40);
        OptionContract putA = ContractFixtures.put("SPY260821P00625000", EXPIRY, 625, -0.07, -0.016, 0.28, 0.32);
        OptionContract putB = ContractFixtures.put("SPY260821P00620000", EXPIRY, 620, -0.05, -0.015, 0.22, 0.26);
        OptionContract itmCall = ContractFixtures.call("SPY260821C00640000", EXPIRY, 640, 0.08, -0.018, 0.35, 0.40);

        List<StrangleCandidate> ranked = strangleService.scan(chainOf(650, call, putA, putB, itmCall), params);

        assertThat(ranked).hasSize(2);
        assertThat(ranked).allMatch(candidate -> candidate.getCall().getStrike() == 680);
        assertThat(ranked).noneMatch(candidate -> candidate.getCall().getStrike() == 640);
        assertThat(ranked.get(0).getRank()).isEqualTo(1);
        assertThat(ranked.get(1).getRank()).isEqualTo(2);
    }

    @Test
    void lowestDeltaDoesNotAutomaticallyRankFirst() {
        OptionContract cheapCall = ContractFixtures.call("CLOW", EXPIRY, 720, 0.05, -0.015, 0.20, 0.22);
        OptionContract cheapPut = ContractFixtures.put("PLOW", EXPIRY, 580, -0.05, -0.015, 0.20, 0.22);
        cheapCall.setVolume(10);
        cheapPut.setVolume(10);
        cheapCall.setOpenInterest(500L);
        cheapPut.setOpenInterest(500L);
        cheapCall.setImpliedVolatility(0.12);
        cheapPut.setImpliedVolatility(0.12);

        OptionContract richCall = ContractFixtures.call("CRICH", EXPIRY, 680, 0.12, -0.03, 0.70, 0.74);
        OptionContract richPut = ContractFixtures.put("PRICH", EXPIRY, 620, -0.12, -0.03, 0.65, 0.69);
        richCall.setVolume(8000);
        richPut.setVolume(8000);
        richCall.setOpenInterest(20000L);
        richPut.setOpenInterest(20000L);
        richCall.setImpliedVolatility(0.28);
        richPut.setImpliedVolatility(0.28);

        OptionChain chain = chainOf(650, cheapCall, cheapPut, richCall, richPut);
        List<StrangleCandidate> ranked = strangleService.scan(chain, params);

        assertThat(ranked).hasSize(4);
        StrangleCandidate winner = ranked.get(0);
        assertThat(winner.getCall().getSymbol()).isEqualTo("CRICH");
        assertThat(winner.getPut().getSymbol()).isEqualTo("PRICH");
        assertThat(winner.getTotalPremium()).isGreaterThan(1.0);
    }

    private OptionChain chainOf(double spot, OptionContract... contracts) {
        OptionChain chain = new OptionChain();
        chain.setUnderlyingSymbol("SPY");
        chain.setUnderlyingPrice(spot);
        chain.setContracts(List.of(contracts));
        return chain;
    }
}
