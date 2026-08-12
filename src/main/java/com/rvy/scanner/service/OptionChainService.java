package com.rvy.scanner.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rvy.scanner.client.AlpacaClient;
import com.rvy.scanner.config.ScannerProperties;
import com.rvy.scanner.model.ExpirationGroup;
import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.model.StrikeRow;

@Service
public class OptionChainService {

    private static final Logger log = LoggerFactory.getLogger(OptionChainService.class);

    private final AlpacaClient alpacaClient;
    private final OptionCalculationService calculations;
    private final ScannerProperties scannerProperties;
    private final EarningsService earningsService;
    private final IvRankService ivRankService;

    public OptionChainService(
            AlpacaClient alpacaClient,
            OptionCalculationService calculations,
            ScannerProperties scannerProperties,
            EarningsService earningsService,
            IvRankService ivRankService) {
        this.alpacaClient = alpacaClient;
        this.calculations = calculations;
        this.scannerProperties = scannerProperties;
        this.earningsService = earningsService;
        this.ivRankService = ivRankService;
    }

    public OptionChain load(String symbol) {
        double price = alpacaClient.fetchUnderlyingPrice(symbol);
        List<OptionContract> contracts = alpacaClient.fetchOptionChain(symbol);
        try {
            alpacaClient.applyOpenInterest(contracts, alpacaClient.fetchOpenInterest(symbol));
        } catch (RuntimeException ex) {
            log.warn("Open interest unavailable for {}: {}", symbol, ex.getMessage());
        }
        OptionChain chain = build(symbol.toUpperCase(), price, contracts);
        try {
            earningsService.apply(chain);
        } catch (RuntimeException ex) {
            log.info("Earnings enrichment skipped for {}: {}", symbol, ex.getMessage());
        }
        try {
            ivRankService.apply(chain);
        } catch (RuntimeException ex) {
            log.info("IV vs HV enrichment skipped for {}: {}", symbol, ex.getMessage());
        }
        return chain;
    }

    public OptionChain build(String symbol, double underlyingPrice, List<OptionContract> contracts) {
        OptionChain chain = new OptionChain();
        chain.setUnderlyingSymbol(symbol);
        chain.setUnderlyingPrice(underlyingPrice);
        chain.setContracts(contracts == null ? List.of() : List.copyOf(contracts));
        chain.setExpirations(groupByExpiration(contracts == null ? List.of() : contracts));
        markExpanded(chain.getExpirations());
        return chain;
    }

    private List<ExpirationGroup> groupByExpiration(List<OptionContract> contracts) {
        Map<LocalDate, List<OptionContract>> byExpiry = new TreeMap<>();
        for (OptionContract contract : contracts) {
            if (contract.getExpiration() == null) {
                continue;
            }
            byExpiry.computeIfAbsent(contract.getExpiration(), key -> new ArrayList<>()).add(contract);
        }

        List<ExpirationGroup> groups = new ArrayList<>();
        for (Map.Entry<LocalDate, List<OptionContract>> entry : byExpiry.entrySet()) {
            ExpirationGroup group = new ExpirationGroup();
            group.setExpiration(entry.getKey());
            group.setDte(calculations.dte(entry.getKey()));
            group.setWeekly(calculations.isWeekly(entry.getKey()));
            group.setContractSize(entry.getValue().stream()
                    .map(OptionContract::getSize)
                    .filter(size -> size > 0)
                    .findFirst()
                    .orElse(100));
            group.setStrikes(toStrikeRows(entry.getValue()));
            groups.add(group);
        }
        return groups;
    }

    private List<StrikeRow> toStrikeRows(List<OptionContract> contracts) {
        Map<Double, StrikeRow> rows = new TreeMap<>();
        for (OptionContract contract : contracts) {
            StrikeRow row = rows.computeIfAbsent(contract.getStrike(), StrikeRow::new);
            if (contract.isCall()) {
                row.setCall(contract);
            } else if (contract.isPut()) {
                row.setPut(contract);
            }
        }
        return new ArrayList<>(rows.values());
    }

    private void markExpanded(List<ExpirationGroup> groups) {
        int minDte = scannerProperties.getDefaults().getMinDte();
        int maxDte = scannerProperties.getDefaults().getMaxDte();
        ExpirationGroup selected = groups.stream()
                .filter(group -> group.getDte() >= minDte && group.getDte() <= maxDte)
                .min(Comparator.comparingInt(ExpirationGroup::getDte))
                .orElseGet(() -> groups.stream()
                        .filter(group -> group.getDte() > 0)
                        .findFirst()
                        .orElse(groups.isEmpty() ? null : groups.get(0)));
        if (selected != null) {
            selected.setExpanded(true);
        }
    }
}
