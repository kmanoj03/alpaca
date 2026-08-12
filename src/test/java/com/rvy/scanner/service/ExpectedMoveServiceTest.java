package com.rvy.scanner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.rvy.scanner.model.ExpectedMove;
import com.rvy.scanner.model.OptionContract;
import com.rvy.scanner.support.ContractFixtures;

class ExpectedMoveServiceTest {

    private final ExpectedMoveService service = new ExpectedMoveService();

    @Test
    void usesAtmStraddleWhenQuotesExist() {
        LocalDate expiry = LocalDate.of(2026, 8, 21);
        OptionContract call = ContractFixtures.call("C", expiry, 650, 0.5, -0.1, 8.0, 8.4);
        OptionContract put = ContractFixtures.put("P", expiry, 650, -0.5, -0.1, 7.8, 8.2);
        call.setImpliedVolatility(0.20);
        put.setImpliedVolatility(0.20);

        ExpectedMove move = service.compute(List.of(call, put), 650, 12);

        assertThat(move.getStraddle()).isCloseTo(16.2, within(0.0001));
        assertThat(move.getUsed()).isEqualTo(move.getStraddle());
        assertThat(move.getImplied()).isPositive();
    }
}
