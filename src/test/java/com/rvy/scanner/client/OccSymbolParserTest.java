package com.rvy.scanner.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.rvy.scanner.model.OptionType;

class OccSymbolParserTest {

    @Test
    void parsesSpyCall() {
        Optional<OccSymbolParser.ParsedOccSymbol> parsed = OccSymbolParser.parse("SPY240821C00680000");
        assertThat(parsed).isPresent();
        assertThat(parsed.get().root()).isEqualTo("SPY");
        assertThat(parsed.get().expiration()).isEqualTo(LocalDate.of(2024, 8, 21));
        assertThat(parsed.get().type()).isEqualTo(OptionType.CALL);
        assertThat(parsed.get().strike()).isEqualTo(680.0);
    }

    @Test
    void parsesFractionalStrikePut() {
        Optional<OccSymbolParser.ParsedOccSymbol> parsed = OccSymbolParser.parse("AAPL240426P00162500");
        assertThat(parsed).isPresent();
        assertThat(parsed.get().type()).isEqualTo(OptionType.PUT);
        assertThat(parsed.get().strike()).isEqualTo(162.5);
    }

    @Test
    void rejectsMalformedSymbols() {
        assertThat(OccSymbolParser.parse(null)).isEmpty();
        assertThat(OccSymbolParser.parse("")).isEmpty();
        assertThat(OccSymbolParser.parse("SPY")).isEmpty();
        assertThat(OccSymbolParser.parse("SPY240821X00680000")).isEmpty();
    }
}
