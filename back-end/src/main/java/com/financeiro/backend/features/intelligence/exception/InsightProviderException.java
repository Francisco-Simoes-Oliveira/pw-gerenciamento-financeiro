package com.financeiro.backend.features.intelligence.exception;

public class InsightProviderException extends RuntimeException {
    public InsightProviderException(String message) { super(message); }
    public InsightProviderException(String message, Throwable cause) { super(message, cause); }
}
