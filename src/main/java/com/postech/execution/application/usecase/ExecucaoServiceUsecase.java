package com.postech.execution.application.usecase;

import com.postech.execution.application.dto.ExecucaoResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ExecucaoServiceUsecase {

    ExecucaoResponseDTO iniciarExecucao(UUID ordemServicoId, UUID clienteId);

    ExecucaoResponseDTO iniciarDiagnostico(UUID id, String notas);

    ExecucaoResponseDTO iniciarReparo(UUID id, String notas);

    ExecucaoResponseDTO finalizar(UUID id, String notas);

    ExecucaoResponseDTO compensar(UUID ordemServicoId);

    ExecucaoResponseDTO buscarPorId(UUID id);

    ExecucaoResponseDTO buscarPorOrdemServico(UUID ordemServicoId);

    List<ExecucaoResponseDTO> listarFila();
}
