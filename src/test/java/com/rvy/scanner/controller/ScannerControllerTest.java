package com.rvy.scanner.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.rvy.scanner.config.ScannerProperties;
import com.rvy.scanner.service.OptionChainService;
import com.rvy.scanner.service.StrangleService;
import com.rvy.scanner.web.FormatSupport;
import com.rvy.scanner.web.StrategyParameterFactory;

@WebMvcTest(controllers = ScannerController.class)
@Import({StrategyParameterFactory.class, ScannerProperties.class, FormatSupport.class})
class ScannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OptionChainService optionChainService;

    @MockitoBean
    private StrangleService strangleService;

    @Test
    void homeRendersScannerView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("scanner"));
    }
}
