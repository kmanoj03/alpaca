package com.rvy.scanner.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import org.springframework.stereotype.Service;

import com.rvy.scanner.model.OptionContract;

@Service
public class OptionCalculationService {

    private final Clock clock;

    public OptionCalculationService(Clock clock) {
        this.clock = clock;
    }

    public LocalDate today() {
        return LocalDate.now(clock);
    }

    public int dte(LocalDate expiration) {
        if (expiration == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(today(), expiration);
    }

    /**
     * Monthly equity options typically expire on the third Friday. Anything else is treated as weekly.
     */
    public boolean isWeekly(LocalDate expiration) {
        return expiration != null && !expiration.equals(thirdFriday(expiration.getYear(), expiration.getMonthValue()));
    }

    public LocalDate thirdFriday(int year, int month) {
        return LocalDate.of(year, month, 1).with(TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.FRIDAY));
    }

    public boolean isOtmCall(double strike, double underlyingPrice) {
        return strike > underlyingPrice;
    }

    public boolean isOtmPut(double strike, double underlyingPrice) {
        return strike < underlyingPrice;
    }

    public boolean isOtm(OptionContract contract, double underlyingPrice) {
        if (contract == null) {
            return false;
        }
        if (contract.isCall()) {
            return isOtmCall(contract.getStrike(), underlyingPrice);
        }
        return isOtmPut(contract.getStrike(), underlyingPrice);
    }

    public double absDelta(OptionContract contract) {
        Double delta = contract.getDelta();
        return delta == null ? 0.0 : Math.abs(delta);
    }

    public double absTheta(OptionContract contract) {
        Double theta = contract.getTheta();
        return theta == null ? 0.0 : Math.abs(theta);
    }

    public Double mid(OptionContract contract) {
        return contract == null ? null : contract.getMid();
    }

    public double totalPremium(double callMid, double putMid) {
        return callMid + putMid;
    }

    public double premiumPerContract(double totalPremium, int contractSize) {
        return totalPremium * contractSize;
    }

    public double lowerBreakeven(double putStrike, double totalPremium) {
        return putStrike - totalPremium;
    }

    public double upperBreakeven(double callStrike, double totalPremium) {
        return callStrike + totalPremium;
    }

    public double callDistancePct(double callStrike, double underlyingPrice) {
        return (callStrike - underlyingPrice) / underlyingPrice;
    }

    public double putDistancePct(double putStrike, double underlyingPrice) {
        return (underlyingPrice - putStrike) / underlyingPrice;
    }
}
