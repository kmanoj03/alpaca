package com.rvy.scanner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rvy.scanner.client.AlpacaClient;
import com.rvy.scanner.config.ScannerProperties;
import com.rvy.scanner.model.ExpirationGroup;
import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.support.ContractFixtures;

@ExtendWith(MockitoExtension.class)
class OptionChainServiceTest {

    @Mock
    private AlpacaClient alpacaClient;

    private OptionChainService chainService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-09T16:00:00Z"), ZoneOffset.UTC);
        ScannerProperties properties = new ScannerProperties();
        chainService = new OptionChainService(
                alpacaClient,
                new OptionCalculationService(clock),
                properties,
                org.mockito.Mockito.mock(com.rvy.scanner.service.EarningsService.class),
                org.mockito.Mockito.mock(com.rvy.scanner.service.IvRankService.class));
    }

    @Test
    void groupsContractsByExpirationAndPairsStrikes() {
        LocalDate weekly = LocalDate.of(2026, 8, 14);
        LocalDate monthly = LocalDate.of(2026, 8, 21);
        List<OptionContract> contracts = List.of(
                ContractFixtures.call("SPY260814C00680000", weekly, 680, 0.08, -0.018, 0.35, 0.40),
                ContractFixtures.put("SPY260814P00625000", weekly, 625, -0.07, -0.016, 0.28, 0.32),
                ContractFixtures.call("SPY260821C00680000", monthly, 680, 0.08, -0.018, 0.35, 0.40),
                ContractFixtures.put("SPY260821P00680000", monthly, 680, -0.07, -0.016, 0.28, 0.32));

        OptionChain chain = chainService.build("SPY", 650, contracts);

        assertThat(chain.getExpirations()).hasSize(2);
        ExpirationGroup first = chain.getExpirations().get(0);
        assertThat(first.getExpiration()).isEqualTo(weekly);
        assertThat(first.getDte()).isEqualTo(5);
        assertThat(first.isWeekly()).isTrue();
        assertThat(first.getHeaderLabel()).contains("(W)");
        assertThat(first.getStrikes()).hasSize(2);
        assertThat(first.getStrikes().get(0).getStrike()).isEqualTo(625.0);
        assertThat(first.getStrikes().get(0).getPut()).isNotNull();
        assertThat(first.getStrikes().get(1).getCall()).isNotNull();
        assertThat(first.getStrikes().get(0).isAtm()).isTrue();
        assertThat(first.getStrikes().get(1).isAtm()).isFalse();

        ExpirationGroup second = chain.getExpirations().get(1);
        assertThat(second.isWeekly()).isFalse();
        assertThat(second.getDte()).isEqualTo(12);
        assertThat(second.isExpanded()).isTrue();
        assertThat(second.getStrikes()).hasSize(1);
        assertThat(second.getStrikes().get(0).getCall()).isNotNull();
        assertThat(second.getStrikes().get(0).getPut()).isNotNull();
    }

    @Test
    void loadJoinsOpenInterestFromAlpaca() {
        LocalDate expiry = LocalDate.of(2026, 8, 21);
        OptionContract call = ContractFixtures.call("SPY260821C00680000", expiry, 680, 0.08, -0.018, 0.35, 0.40);
        when(alpacaClient.fetchUnderlyingPrice("SPY")).thenReturn(650.0);
        when(alpacaClient.fetchOptionChain("SPY")).thenReturn(List.of(call));
        when(alpacaClient.fetchOpenInterest("SPY")).thenReturn(java.util.Map.of("SPY260821C00680000", 1500L));

        OptionChain chain = chainService.load("SPY");

        org.mockito.Mockito.verify(alpacaClient).applyOpenInterest(
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.eq(java.util.Map.of("SPY260821C00680000", 1500L)));
        assertThat(chain.getUnderlyingPrice()).isEqualTo(650.0);
        assertThat(chain.getExpirations()).hasSize(1);
    }
}
