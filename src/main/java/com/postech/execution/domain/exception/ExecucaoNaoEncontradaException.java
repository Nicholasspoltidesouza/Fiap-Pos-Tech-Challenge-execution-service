package com.postech.execution.domain.exception;

import java.util.UUID;

public class ExecucaoNaoEncontradaException extends RuntimeException {

    public ExecucaoNaoEncontradaException(UUID id) {
        super("Execution job not found: " + id);
    }

    public ExecucaoNaoEncontradaException(String message) {
        super(message);
    }
}
