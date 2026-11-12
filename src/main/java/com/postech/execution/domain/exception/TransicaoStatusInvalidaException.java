package com.postech.execution.domain.exception;

import com.postech.execution.domain.model.StatusExecucao;

public class TransicaoStatusInvalidaException extends RuntimeException {

    public TransicaoStatusInvalidaException(StatusExecucao atual, StatusExecucao destino) {
        super("Invalid status transition from " + atual + " to " + destino);
    }
}
