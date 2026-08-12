package com.rvy.scanner.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rvy.scanner.model.ExpectedMove;
import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.StrangleCandidate;
import com.rvy.scanner.model.StrategyParameters;

@Service
public class StrangleService {

    private static final int MAX_PAIRS_PER_EXPIRATION = 250;

    private final OptionFilterService filterService;
    private final OptionCalculationService calculations;
    private final ExpectedMoveService expectedMoveService;
    private final StrangleRankingService rankingService;

    public StrangleService(
            OptionFilterService filterService,
            OptionCalculationService calculations,
            ExpectedMoveService expectedMoveService,
            StrangleRankingService rankingService) {
        this.filterService = filterService;
        this.calculations = calculations;
        this.expectedMoveService = expectedMoveService;
        this.rankingService = rankingService;
    }

    public List<StrangleCandidate> scan(OptionChain chain, StrategyParameters params) {
        if (chain == null || chain.getContracts() == null || chain.getContracts().isEmpty()) {
            return List.of();
        }

        List<OptionContract> filtered = filterService.filterForScanner(
                chain.getContracts(), chain.getUnderlyingPrice(), params);
        Map<LocalDate, List<OptionContract>> allByExpiry = groupByExpiration(chain.getContracts());
        Map<LocalDate, List<OptionContract>> filteredByExpiry = groupByExpiration(filtered);

        List<StrangleCandidate> candidates = new ArrayList<>();
        for (Map.Entry<LocalDate, List<OptionContract>> entry : filteredByExpiry.entrySet()) {
            LocalDate expiration = entry.getKey();
            List<OptionContract> calls = filterService.calls(entry.getValue());
            List<OptionContract> puts = filterService.puts(entry.getValue());
            if (calls.isEmpty() || puts.isEmpty()) {
                continue;
            }
            int dte = calculations.dte(expiration);
            ExpectedMove expectedMove = expectedMoveService.compute(
                    allByExpiry.getOrDefault(expiration, List.of()),
                    chain.getUnderlyingPrice(),
                    dte);
            int pairs = 0;
            for (OptionContract call : calls) {
                for (OptionContract put : puts) {
                    candidates.add(toCandidate(chain, call, put, expectedMove, dte));
                    pairs++;
                    if (pairs >= MAX_PAIRS_PER_EXPIRATION) {
                        break;
                    }
                }
                if (pairs >= MAX_PAIRS_PER_EXPIRATION) {
                    break;
                }
            }
        }
        List<StrangleCandidate> ranked = rankingService.rank(candidates, params);
        annotate(chain, ranked);
        return ranked;
    }

    private void annotate(OptionChain chain, List<StrangleCandidate> candidates) {
        for (StrangleCandidate candidate : candidates) {
            candidate.setIvVsHvPercentile(chain.getIvVsHvPercentile());
            chain.getExpirations().stream()
                    .filter(group -> group.getExpiration().equals(candidate.getExpiration()))
                    .findFirst()
                    .ifPresent(group -> {
                        candidate.setEarningsBeforeExpiration(group.isEarningsBeforeExpiration());
                        candidate.setEarningsDate(group.getEarningsDate());
                    });
        }
    }

    public StrangleCandidate toCandidate(
            OptionChain chain,
            OptionContract call,
            OptionContract put,
            ExpectedMove expectedMove,
            int dte) {
        double callMid = call.getMid();
        double putMid = put.getMid();
        double totalPremium = calculations.totalPremium(callMid, putMid);
        int size = call.getSize() > 0 ? call.getSize() : 100;

        StrangleCandidate candidate = new StrangleCandidate();
        candidate.setId(call.getSymbol() + "_" + put.getSymbol());
        candidate.setUnderlyingSymbol(chain.getUnderlyingSymbol());
        candidate.setUnderlyingPrice(chain.getUnderlyingPrice());
        candidate.setExpiration(call.getExpiration());
        candidate.setDte(dte);
        candidate.setWeekly(calculations.isWeekly(call.getExpiration()));
        candidate.setCall(call);
        candidate.setPut(put);
        candidate.setTotalPremium(totalPremium);
        candidate.setPremiumPerContract(calculations.premiumPerContract(totalPremium, size));
        candidate.setLowerBreakeven(calculations.lowerBreakeven(put.getStrike(), totalPremium));
        candidate.setUpperBreakeven(calculations.upperBreakeven(call.getStrike(), totalPremium));
        candidate.setCallDistancePct(calculations.callDistancePct(call.getStrike(), chain.getUnderlyingPrice()));
        candidate.setPutDistancePct(calculations.putDistancePct(put.getStrike(), chain.getUnderlyingPrice()));
        candidate.setExpectedMove(expectedMove);
        candidate.setAverageIv(averageIv(call, put));
        return candidate;
    }

    public StrangleCandidate findById(List<StrangleCandidate> candidates, String id) {
        if (candidates == null || id == null) {
            return null;
        }
        return candidates.stream().filter(candidate -> id.equals(candidate.getId())).findFirst().orElse(null);
    }

    private Map<LocalDate, List<OptionContract>> groupByExpiration(List<OptionContract> contracts) {
        Map<LocalDate, List<OptionContract>> grouped = new LinkedHashMap<>();
        for (OptionContract contract : contracts) {
            if (contract.getExpiration() == null) {
                continue;
            }
            grouped.computeIfAbsent(contract.getExpiration(), key -> new ArrayList<>()).add(contract);
        }
        return grouped;
    }

    private Double averageIv(OptionContract call, OptionContract put) {
        int count = 0;
        double sum = 0;
        if (call.getImpliedVolatility() != null) {
            sum += call.getImpliedVolatility();
            count++;
        }
        if (put.getImpliedVolatility() != null) {
            sum += put.getImpliedVolatility();
            count++;
        }
        return count == 0 ? null : sum / count;
    }
}
