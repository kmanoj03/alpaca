package com.rvy.scanner.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.StrangleCandidate;
import com.rvy.scanner.model.StrategyParameters;
import com.rvy.scanner.service.HistoryService;
import com.rvy.scanner.service.OptionChainService;
import com.rvy.scanner.service.StrangleService;
import com.rvy.scanner.web.StrategyParameterFactory;
import com.rvy.scanner.web.StrikeVisualization;

@Controller
public class ScannerController {

    private final OptionChainService optionChainService;
    private final StrangleService strangleService;
    private final StrategyParameterFactory parameterFactory;
    private final HistoryService historyService;

    public ScannerController(
            OptionChainService optionChainService,
            StrangleService strangleService,
            StrategyParameterFactory parameterFactory,
            HistoryService historyService) {
        this.optionChainService = optionChainService;
        this.strangleService = strangleService;
        this.parameterFactory = parameterFactory;
        this.historyService = historyService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("symbol", "SPY");
        model.addAttribute("params", parameterFactory.defaults());
        return "scanner";
    }

    @GetMapping("/scan")
    public String scan(
            @RequestParam String symbol,
            @RequestParam(required = false) Double minDelta,
            @RequestParam(required = false) Double maxDelta,
            @RequestParam(required = false) Double minTheta,
            @RequestParam(required = false) Integer minDte,
            @RequestParam(required = false) Integer maxDte,
            @RequestParam(required = false) Double minPremium,
            @RequestParam(required = false) Double maxSpread,
            @RequestParam(required = false) Integer minOpenInterest,
            @RequestParam(required = false) Integer minVolume,
            Model model) {
        StrategyParameters params = parameterFactory.from(
                minDelta, maxDelta, minTheta, minDte, maxDte, minPremium, maxSpread, minOpenInterest, minVolume);
        OptionChain chain = optionChainService.load(symbol);
        List<StrangleCandidate> candidates = strangleService.scan(chain, params);
        historyService.save(chain, candidates);
        java.util.Map<String, StrikeVisualization> vizById = new java.util.LinkedHashMap<>();
        for (StrangleCandidate candidate : candidates) {
            vizById.put(candidate.getId(), new StrikeVisualization(candidate));
        }
        model.addAttribute("symbol", symbol.toUpperCase());
        model.addAttribute("params", params);
        model.addAttribute("chain", chain);
        model.addAttribute("candidates", candidates);
        model.addAttribute("vizById", vizById);
        if (!candidates.isEmpty()) {
            model.addAttribute("selected", candidates.get(0));
        }
        return "scanner";
    }
}
