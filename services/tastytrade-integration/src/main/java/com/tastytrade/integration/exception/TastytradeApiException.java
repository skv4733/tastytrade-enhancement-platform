package com.tastytrade.integration.exception;

public class TastytradeApiException extends RuntimeException {
    
    public TastytradeApiException(String message) {
        super(message);
    }
    
    public TastytradeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}