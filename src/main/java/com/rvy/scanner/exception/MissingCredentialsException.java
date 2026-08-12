package com.rvy.scanner.exception;

public class MissingCredentialsException extends RuntimeException {

    public MissingCredentialsException() {
        super("Alpaca API credentials are not configured. Set ALPACA_API_KEY and ALPACA_API_SECRET.");
    }
}
