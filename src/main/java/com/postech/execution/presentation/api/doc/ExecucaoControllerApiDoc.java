package com.postech.execution.presentation.api.doc;

import com.postech.execution.application.dto.ExecucaoResponseDTO;
import com.postech.execution.application.dto.FinalizarExecucaoRequestDTO;
import com.postech.execution.application.dto.NotasRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Execucoes", description = "Workshop floor execution queue (PostgreSQL execution_db)")
public interface ExecucaoControllerApiDoc {

    @Operation(summary = "List jobs waiting in the execution queue")
    ResponseEntity<List<ExecucaoResponseDTO>> listarFila();

    @Operation(summary = "Get execution job by id")
    ResponseEntity<ExecucaoResponseDTO> buscarPorId(UUID id);

    @Operation(summary = "Get latest execution job by service order id")
    ResponseEntity<ExecucaoResponseDTO> buscarPorOrdem(UUID ordemServicoId);

    @Operation(
            summary = "Start diagnosis for a queued job",
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject(
                    name = "Diagnostico",
                    value = "{\"notas\":\"Correia dentada danificada\"}"))))
    ResponseEntity<ExecucaoResponseDTO> iniciarDiagnostico(UUID id, NotasRequestDTO request);

    @Operation(
            summary = "Start repair after diagnosis",
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject(
                    name = "Reparo",
                    value = "{\"notas\":\"Substituicao da correia e tensionador\"}"))))
    ResponseEntity<ExecucaoResponseDTO> iniciarReparo(UUID id, NotasRequestDTO request);

    @Operation(
            summary = "Finish execution and notify OS Service",
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject(
                    name = "Finalizar",
                    value = "{\"notas\":\"Servico concluido e testado\"}"))))
    ResponseEntity<ExecucaoResponseDTO> finalizar(UUID id, FinalizarExecucaoRequestDTO request);
}
