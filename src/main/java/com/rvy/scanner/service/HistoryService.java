package com.rvy.scanner.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rvy.scanner.client.AlpacaClient;
import com.rvy.scanner.entity.SavedCandidate;
import com.rvy.scanner.entity.SavedScan;
import com.rvy.scanner.model.OptionChain;
import com.rvy.scanner.model.StockBar;
import com.rvy.scanner.model.StrangleCandidate;
import com.rvy.scanner.repository.SavedScanRepository;

@Service
public class HistoryService {

    private static final Logger log = LoggerFactory.getLogger(HistoryService.class);

    private final SavedScanRepository scanRepository;
    private final AlpacaClient alpacaClient;
    private final OptionCalculationService calculations;
    private final PnlCalculator pnlCalculator = new PnlCalculator();
    private final Clock clock;

    public HistoryService(
            SavedScanRepository scanRepository,
            AlpacaClient alpacaClient,
            OptionCalculationService calculations,
            Clock clock) {
        this.scanRepository = scanRepository;
        this.alpacaClient = alpacaClient;
        this.calculations = calculations;
        this.clock = clock;
    }

    @Transactional
    public SavedScan save(OptionChain chain, List<StrangleCandidate> candidates) {
        if (chain == null || candidates == null || candidates.isEmpty()) {
            return null;
        }
        SavedScan scan = new SavedScan();
        scan.setScannedAt(Instant.now(clock));
        scan.setSymbol(chain.getUnderlyingSymbol());
        scan.setUnderlyingPrice(chain.getUnderlyingPrice());
        List<SavedCandidate> rows = new ArrayList<>();
        for (StrangleCandidate candidate : candidates) {
            SavedCandidate row = new SavedCandidate();
            row.setScan(scan);
            row.setCandidateKey(candidate.getId());
            row.setExpiration(candidate.getExpiration());
            row.setDte(candidate.getDte());
            row.setPutStrike(candidate.getPutStrike());
            row.setCallStrike(candidate.getCallStrike());
            row.setTotalPremium(candidate.getTotalPremium());
            row.setPremiumPerContract(candidate.getPremiumPerContract());
            row.setLowerBreakeven(candidate.getLowerBreakeven());
            row.setUpperBreakeven(candidate.getUpperBreakeven());
            row.setScore(candidate.getScore());
            row.setEarningsBeforeExpiration(candidate.isEarningsBeforeExpiration());
            rows.add(row);
        }
        scan.setCandidates(rows);
        return scanRepository.save(scan);
    }

    @Transactional
    public List<SavedScan> loadEvaluated() {
        List<SavedScan> scans = scanRepository.findAllByOrderByScannedAtDesc();
        for (SavedScan scan : scans) {
            evaluate(scan);
        }
        return scans;
    }

    private void evaluate(SavedScan scan) {
        try {
            double current = alpacaClient.fetchUnderlyingPrice(scan.getSymbol());
            LocalDate today = calculations.today();
            LocalDate start = scan.getScannedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate();
            List<StockBar> bars = alpacaClient.fetchDailyBars(scan.getSymbol(), start, today);
            Instant now = Instant.now(clock);
            for (SavedCandidate candidate : scan.getCandidates()) {
                List<StockBar> after = bars.stream()
                        .filter(bar -> !bar.getDate().isBefore(start))
                        .toList();
                PnlCalculator.Evaluation evaluation = pnlCalculator.evaluate(
                        candidate.getExpiration(),
                        candidate.getPutStrike(),
                        candidate.getCallStrike(),
                        candidate.getTotalPremium(),
                        100,
                        scan.getUnderlyingPrice(),
                        current,
                        today,
                        after);
                candidate.setLatestUnderlying(current);
                candidate.setStayedBetweenStrikes(evaluation.stayedBetweenStrikes());
                candidate.setTheoreticalPl(evaluation.theoreticalPl());
                candidate.setMaxAdverseMove(evaluation.maxAdverseMove());
                candidate.setExpired(evaluation.expired());
                candidate.setEvaluatedAt(now);
            }
        } catch (RuntimeException ex) {
            log.info("Could not evaluate history for {}: {}", scan.getSymbol(), ex.getMessage());
        }
    }
}
