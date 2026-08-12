package com.rvy.scanner.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rvy.scanner.model.ExpectedMove;
import com.rvy.scanner.model.OptionContract;

@Service
public class ExpectedMoveService {

    public ExpectedMove compute(List<OptionContract> expirationContracts, double underlyingPrice, int dte) {
        ExpectedMove move = new ExpectedMove();
        if (expirationContracts == null || expirationContracts.isEmpty() || underlyingPrice <= 0) {
            return move;
        }

        OptionContract atmCall = nearest(expirationContracts, underlyingPrice, true);
        OptionContract atmPut = nearest(expirationContracts, underlyingPrice, false);
        if (atmCall != null && atmPut != null && atmCall.getMid() != null && atmPut.getMid() != null) {
            move.setStraddle(atmCall.getMid() + atmPut.getMid());
        }

        Double atmIv = firstIv(atmCall, atmPut);
        if (atmIv != null && dte > 0) {
            move.setImplied(underlyingPrice * atmIv * Math.sqrt(dte / 365.0));
        }

        if (move.getStraddle() != null && move.getStraddle() > 0) {
            move.setUsed(move.getStraddle());
        } else {
            move.setUsed(move.getImplied());
        }
        return move;
    }

    private OptionContract nearest(List<OptionContract> contracts, double spot, boolean call) {
        return contracts.stream()
                .filter(contract -> call ? contract.isCall() : contract.isPut())
                .min(Comparator.comparingDouble(contract -> Math.abs(contract.getStrike() - spot)))
                .orElse(null);
    }

    private Double firstIv(OptionContract call, OptionContract put) {
        if (call != null && call.getImpliedVolatility() != null) {
            return call.getImpliedVolatility();
        }
        if (put != null && put.getImpliedVolatility() != null) {
            return put.getImpliedVolatility();
        }
        return null;
    }
}
