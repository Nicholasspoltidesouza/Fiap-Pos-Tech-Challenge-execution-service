package com.postech.execution.domain.exception;

import java.util.UUID;

public class ExecucaoJaExisteException extends RuntimeException {

    public ExecucaoJaExisteException(UUID ordemServicoId) {
        super("Active execution already exists for service order: " + ordemServicoId);
    }
}
