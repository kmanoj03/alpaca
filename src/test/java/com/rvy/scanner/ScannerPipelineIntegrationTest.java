package com.rvy.scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rvy.scanner.client.AlpacaClient;
import com.rvy.scanner.client.EarningsClient;
import com.rvy.scanner.config.ScannerProperties;
import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.OptionType;
import com.rvy.scanner.model.StrangleCandidate;
import com.rvy.scanner.model.StrategyParameters;
import com.rvy.scanner.service.EarningsService;
import com.rvy.scanner.service.ExpectedMoveService;
import com.rvy.scanner.service.IvRankService;
import com.rvy.scanner.service.OptionCalculationService;
import com.rvy.scanner.service.OptionChainService;
import com.rvy.scanner.service.OptionFilterService;
import com.rvy.scanner.service.StrangleRankingService;
import com.rvy.scanner.service.StrangleService;
import com.rvy.scanner.support.ContractFixtures;

@ExtendWith(MockitoExtension.class)
class ScannerPipelineIntegrationTest {

    private static final LocalDate AUG_14 = LocalDate.of(2026, 8, 14);
    private static final LocalDate AUG_21 = LocalDate.of(2026, 8, 21);

    @Mock
    private AlpacaClient alpacaClient;

    @Mock
    private EarningsClient earningsClient;

    private OptionChainService chainService;
    private StrangleService strangleService;
    private StrategyParameters params;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-09T16:00:00Z"), ZoneOffset.UTC);
        OptionCalculationService calculations = new OptionCalculationService(clock);
        ScannerProperties properties = new ScannerProperties();
        chainService = new OptionChainService(
                alpacaClient,
                calculations,
                properties,
                new EarningsService(earningsClient, calculations),
                new IvRankService(alpacaClient, calculations));
        strangleService = new StrangleService(
                new OptionFilterService(calculations),
                calculations,
                new ExpectedMoveService(),
                new StrangleRankingService(calculations, properties));
        params = ContractFixtures.defaultParams();
    }

    @Test
    void spyPipelineFiltersPairsAndRanksRecommendedStrangles() {
        OptionContract assignmentCall = ContractFixtures.call(
                "SPY260821C00680000", AUG_21, 680, 0.08, -0.018, 0.35, 0.40);
        OptionContract assignmentPut = ContractFixtures.put(
                "SPY260821P00625000", AUG_21, 625, -0.07, -0.016, 0.28, 0.32);
        OptionContract secondPut = ContractFixtures.put(
                "SPY260821P00620000", AUG_21, 620, -0.05, -0.015, 0.22, 0.26);
        OptionContract itmCall = ContractFixtures.call(
                "SPY260821C00640000", AUG_21, 640, 0.08, -0.018, 0.35, 0.40);
        OptionContract missingGreeks = ContractFixtures.contract(
                "SPY260821C00700000", OptionType.CALL, AUG_21, 700, null, null, 0.40, 0.45, 900, 800L);
        OptionContract weeklyCall = ContractFixtures.call(
                "SPY260814C00680000", AUG_14, 680, 0.08, -0.018, 0.35, 0.40);
        OptionContract weeklyPut = ContractFixtures.put(
                "SPY260814P00625000", AUG_14, 625, -0.07, -0.016, 0.28, 0.32);
        assignmentCall.setImpliedVolatility(0.18);
        assignmentPut.setImpliedVolatility(0.18);

        List<OptionContract> contracts = List.of(
                assignmentCall, assignmentPut, secondPut, itmCall, missingGreeks, weeklyCall, weeklyPut);

        when(alpacaClient.fetchUnderlyingPrice("SPY")).thenReturn(650.0);
        when(alpacaClient.fetchOptionChain("SPY")).thenReturn(contracts);
        when(alpacaClient.fetchOpenInterest("SPY")).thenReturn(Map.of(
                "SPY260821C00680000", 1200L,
                "SPY260821P00625000", 1100L));
        doNothing().when(alpacaClient).applyOpenInterest(anyList(), any());
        when(earningsClient.nextEarningsDate("SPY")).thenReturn(Optional.empty());
        when(alpacaClient.fetchDailyBars(eq("SPY"), any(), any())).thenReturn(List.of());

        OptionChain chain = chainService.load("SPY");
        List<StrangleCandidate> recommended = strangleService.scan(chain, params);

        assertThat(chain.getUnderlyingPrice()).isEqualTo(650.0);
        assertThat(chain.getExpirations()).hasSize(2);
        assertThat(chain.getContracts()).hasSize(7);
        assertThat(chain.getExpirations().get(0).isWeekly()).isTrue();
        assertThat(chain.getExpirations().get(0).getDte()).isEqualTo(5);
        assertThat(chain.getExpirations().get(1).getDte()).isEqualTo(12);
        assertThat(chain.getExpirations().get(1).isExpanded()).isTrue();

        assertThat(recommended).hasSize(2);
        assertThat(recommended).allMatch(candidate -> candidate.getExpiration().equals(AUG_21));
        assertThat(recommended).noneMatch(candidate -> candidate.getCall().getStrike() == 640);
        assertThat(recommended).noneMatch(candidate -> candidate.getCall().getGreeks() == null);

        StrangleCandidate assignment = recommended.stream()
                .filter(candidate -> candidate.getPutStrike() == 625.0)
                .findFirst()
                .orElseThrow();
        assertThat(assignment.getTotalPremium()).isEqualTo(0.675);
        assertThat(assignment.getPremiumPerContract()).isEqualTo(67.5);
        assertThat(assignment.getLowerBreakeven()).isEqualTo(624.325);
        assertThat(assignment.getUpperBreakeven()).isEqualTo(680.675);
        assertThat(assignment.getRank()).isBetween(1, 2);
        assertThat(assignment.getScore()).isPositive();
    }
}
