package com.rvy.scanner.client;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rvy.scanner.model.OptionType;

/**
 * Parses compact OCC option symbols used by Alpaca, e.g. {@code SPY240821C00680000}.
 */
public final class OccSymbolParser {

    private static final Pattern OCC = Pattern.compile("^([A-Z]+)(\\d{6})([CP])(\\d{8})$");
    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");

    private OccSymbolParser() {
    }

    public static Optional<ParsedOccSymbol> parse(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = OCC.matcher(symbol.trim().toUpperCase());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            LocalDate expiration = LocalDate.parse(matcher.group(2), YYMMDD);
            OptionType type = OptionType.fromOcc(matcher.group(3).charAt(0));
            double strike = Long.parseLong(matcher.group(4)) / 1000.0;
            return Optional.of(new ParsedOccSymbol(matcher.group(1), expiration, type, strike));
        } catch (DateTimeParseException | NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public record ParsedOccSymbol(String root, LocalDate expiration, OptionType type, double strike) {
    }
}
