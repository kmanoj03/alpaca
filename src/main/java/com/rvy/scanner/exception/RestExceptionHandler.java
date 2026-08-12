package com.rvy.scanner.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rvy.scanner.controller.OptionApiController;

@RestControllerAdvice(assignableTypes = OptionApiController.class)
public class RestExceptionHandler {

    @ExceptionHandler(MissingCredentialsException.class)
    public ResponseEntity<Map<String, Object>> missingCredentials(MissingCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(InvalidSymbolException.class)
    public ResponseEntity<Map<String, Object>> invalidSymbol(InvalidSymbolException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MissingDataException.class)
    public ResponseEntity<Map<String, Object>> missingData(MissingDataException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AlpacaApiException.class)
    public ResponseEntity<Map<String, Object>> alpaca(AlpacaApiException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return error(status, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message, "status", status.value()));
    }
}
