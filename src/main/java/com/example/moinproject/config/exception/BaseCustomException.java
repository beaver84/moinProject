package com.example.moinproject.config.exception;

public abstract class BaseCustomException extends RuntimeException {
    public BaseCustomException(String message) {
        super(message);
    }
}
