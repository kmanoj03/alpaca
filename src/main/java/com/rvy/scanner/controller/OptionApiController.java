package com.rvy.scanner.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.StrangleCandidate;
import com.rvy.scanner.model.StrategyParameters;
import com.rvy.scanner.service.OptionChainService;
import com.rvy.scanner.service.StrangleService;
import com.rvy.scanner.web.StrategyParameterFactory;

@RestController
@RequestMapping("/api/options")
public class OptionApiController {

    private final OptionChainService optionChainService;
    private final StrangleService strangleService;
    private final StrategyParameterFactory parameterFactory;

    public OptionApiController(
            OptionChainService optionChainService,
            StrangleService strangleService,
            StrategyParameterFactory parameterFactory) {
        this.optionChainService = optionChainService;
        this.strangleService = strangleService;
        this.parameterFactory = parameterFactory;
    }

    @GetMapping("/chain/{symbol}")
    public OptionChain chain(@PathVariable String symbol) {
        return optionChainService.load(symbol);
    }

    @GetMapping("/strangle/{symbol}")
    public List<StrangleCandidate> strangles(
            @PathVariable String symbol,
            @RequestParam(required = false) Double minDelta,
            @RequestParam(required = false) Double maxDelta,
            @RequestParam(required = false) Double minTheta,
            @RequestParam(required = false) Integer minDte,
            @RequestParam(required = false) Integer maxDte,
            @RequestParam(required = false) Double minPremium,
            @RequestParam(required = false) Double maxSpread,
            @RequestParam(required = false) Integer minOpenInterest,
            @RequestParam(required = false) Integer minVolume) {
        StrategyParameters params = parameterFactory.from(
                minDelta, maxDelta, minTheta, minDte, maxDte, minPremium, maxSpread, minOpenInterest, minVolume);
        OptionChain chain = optionChainService.load(symbol);
        return strangleService.scan(chain, params);
    }

    @GetMapping("/strangle/{symbol}/{id}")
    public StrangleCandidate strangleDetail(
            @PathVariable String symbol,
            @PathVariable String id,
            @RequestParam(required = false) Double minDelta,
            @RequestParam(required = false) Double maxDelta,
            @RequestParam(required = false) Double minTheta,
            @RequestParam(required = false) Integer minDte,
            @RequestParam(required = false) Integer maxDte,
            @RequestParam(required = false) Double minPremium,
            @RequestParam(required = false) Double maxSpread,
            @RequestParam(required = false) Integer minOpenInterest,
            @RequestParam(required = false) Integer minVolume) {
        List<StrangleCandidate> candidates = strangles(
                symbol, minDelta, maxDelta, minTheta, minDte, maxDte, minPremium, maxSpread, minOpenInterest, minVolume);
        StrangleCandidate match = strangleService.findById(candidates, id);
        if (match == null) {
            throw new com.rvy.scanner.exception.MissingDataException("No strangle candidate with id " + id);
        }
        return match;
    }
}
