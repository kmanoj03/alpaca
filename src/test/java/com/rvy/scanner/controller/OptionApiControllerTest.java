package com.rvy.scanner.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.rvy.scanner.config.ScannerProperties;
import com.rvy.scanner.exception.MissingCredentialsException;
import com.rvy.scanner.exception.RestExceptionHandler;
import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.StrangleCandidate;
import com.rvy.scanner.service.HistoryService;
import com.rvy.scanner.service.OptionChainService;
import com.rvy.scanner.service.StrangleService;
import com.rvy.scanner.support.ContractFixtures;
import com.rvy.scanner.web.FormatSupport;
import com.rvy.scanner.web.StrategyParameterFactory;

@WebMvcTest(controllers = OptionApiController.class)
@Import({StrategyParameterFactory.class, ScannerProperties.class, RestExceptionHandler.class, FormatSupport.class})
class OptionApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OptionChainService optionChainService;

    @MockitoBean
    private StrangleService strangleService;

    @MockitoBean
    private HistoryService historyService;

    @Test
    void chainEndpointReturnsUnderlying() throws Exception {
        OptionChain chain = new OptionChain();
        chain.setUnderlyingSymbol("SPY");
        chain.setUnderlyingPrice(650.0);
        when(optionChainService.load("SPY")).thenReturn(chain);

        mockMvc.perform(get("/api/options/chain/SPY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.underlyingSymbol").value("SPY"))
                .andExpect(jsonPath("$.underlyingPrice").value(650.0));
    }

    @Test
    void strangleEndpointReturnsRankedCandidates() throws Exception {
        OptionChain chain = new OptionChain();
        chain.setUnderlyingSymbol("SPY");
        chain.setUnderlyingPrice(650.0);
        OptionContract call = ContractFixtures.call("C", LocalDate.of(2026, 8, 21), 680, 0.08, -0.018, 0.35, 0.40);
        OptionContract put = ContractFixtures.put("P", LocalDate.of(2026, 8, 21), 625, -0.07, -0.016, 0.28, 0.32);
        StrangleCandidate candidate = new StrangleCandidate();
        candidate.setId("C_P");
        candidate.setRank(1);
        candidate.setCall(call);
        candidate.setPut(put);
        candidate.setTotalPremium(0.675);
        when(optionChainService.load("SPY")).thenReturn(chain);
        when(strangleService.scan(eq(chain), any())).thenReturn(List.of(candidate));

        mockMvc.perform(get("/api/options/strangle/SPY").param("minDelta", "0.05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalPremium").value(0.675))
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    void chainEndpointMapsMissingCredentials() throws Exception {
        when(optionChainService.load("SPY")).thenThrow(new MissingCredentialsException());

        mockMvc.perform(get("/api/options/chain/SPY"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }
}
