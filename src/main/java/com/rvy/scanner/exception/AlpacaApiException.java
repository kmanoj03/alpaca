package com.rvy.scanner.exception;

import org.springframework.http.HttpStatusCode;

public class AlpacaApiException extends RuntimeException {

    private final int statusCode;

    public AlpacaApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public AlpacaApiException(HttpStatusCode status, String body) {
        super(describe(status.value(), body));
        this.statusCode = status.value();
    }

    public int getStatusCode() {
        return statusCode;
    }

    private static String describe(int status, String body) {
        String detail = (body == null || body.isBlank()) ? "" : ": " + body;
        return switch (status) {
            case 400 -> "Invalid Alpaca request" + detail;
            case 401 -> "Invalid or missing Alpaca API credentials";
            case 403 -> "Alpaca access forbidden" + detail;
            case 429 -> "Alpaca rate limit exceeded";
            case 500 -> "Alpaca server error" + detail;
            default -> "Alpaca API error (" + status + ")" + detail;
        };
    }
}
