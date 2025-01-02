package com.example.moinproject.config.exception;

public class DailyLimitExceededException extends BaseCustomException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}