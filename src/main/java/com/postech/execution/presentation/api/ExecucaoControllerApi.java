package com.postech.execution.presentation.api;

import com.postech.execution.application.dto.ExecucaoResponseDTO;
import com.postech.execution.application.dto.FinalizarExecucaoRequestDTO;
import com.postech.execution.application.dto.NotasRequestDTO;
import com.postech.execution.application.usecase.ExecucaoServiceUsecase;
import com.postech.execution.presentation.api.doc.ExecucaoControllerApiDoc;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/execucoes")
public class ExecucaoControllerApi implements ExecucaoControllerApiDoc {

    private final ExecucaoServiceUsecase execucaoService;

    public ExecucaoControllerApi(ExecucaoServiceUsecase execucaoService) {
        this.execucaoService = execucaoService;
    }

    @Override
    @GetMapping("/fila")
    public ResponseEntity<List<ExecucaoResponseDTO>> listarFila() {
        return ResponseEntity.ok(execucaoService.listarFila());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ExecucaoResponseDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(execucaoService.buscarPorId(id));
    }

    @Override
    @GetMapping("/por-ordem/{ordemServicoId}")
    public ResponseEntity<ExecucaoResponseDTO> buscarPorOrdem(@PathVariable UUID ordemServicoId) {
        return ResponseEntity.ok(execucaoService.buscarPorOrdemServico(ordemServicoId));
    }

    @Override
    @PostMapping("/{id}/iniciar-diagnostico")
    public ResponseEntity<ExecucaoResponseDTO> iniciarDiagnostico(
            @PathVariable UUID id,
            @RequestBody(required = false) NotasRequestDTO request) {
        String notas = request == null ? null : request.notas();
        return ResponseEntity.ok(execucaoService.iniciarDiagnostico(id, notas));
    }

    @Override
    @PostMapping("/{id}/iniciar-reparo")
    public ResponseEntity<ExecucaoResponseDTO> iniciarReparo(
            @PathVariable UUID id,
            @RequestBody(required = false) NotasRequestDTO request) {
        String notas = request == null ? null : request.notas();
        return ResponseEntity.ok(execucaoService.iniciarReparo(id, notas));
    }

    @Override
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ExecucaoResponseDTO> finalizar(
            @PathVariable UUID id,
            @RequestBody(required = false) FinalizarExecucaoRequestDTO request) {
        String notas = request == null ? null : request.notas();
        return ResponseEntity.ok(execucaoService.finalizar(id, notas));
    }
}
