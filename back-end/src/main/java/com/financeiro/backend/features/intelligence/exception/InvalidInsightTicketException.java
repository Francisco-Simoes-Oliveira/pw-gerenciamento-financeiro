package com.financeiro.backend.features.intelligence.exception;

public class InvalidInsightTicketException extends RuntimeException {
    public InvalidInsightTicketException() {
        super("Ticket de análise inválido, expirado ou já utilizado.");
    }
}
