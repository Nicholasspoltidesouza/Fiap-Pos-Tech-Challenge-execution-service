package com.postech.execution.application.dto;

import com.postech.execution.domain.model.StatusExecucao;

import java.time.Instant;
import java.util.UUID;

public record ExecucaoResponseDTO(
        UUID id,
        UUID ordemServicoId,
        UUID clienteId,
        StatusExecucao status,
        Integer posicaoFila,
        String diagnosticoNotas,
        String reparoNotas,
        Instant createdAt,
        Instant updatedAt,
        Instant finalizadoEm
) {
}
