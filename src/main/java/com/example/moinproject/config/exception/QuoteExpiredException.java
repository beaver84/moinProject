package com.example.moinproject.config.exception;

public class QuoteExpiredException extends RuntimeException {
    public QuoteExpiredException(String message) {
        super(message);
    }
}
