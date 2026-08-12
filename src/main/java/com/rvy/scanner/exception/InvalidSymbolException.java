package com.rvy.scanner.exception;

public class InvalidSymbolException extends RuntimeException {

    public InvalidSymbolException(String symbol) {
        super("Invalid or unsupported underlying symbol: " + symbol);
    }
}
