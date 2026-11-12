package com.postech.execution.presentation.api;

import com.postech.execution.application.dto.ExecucaoResponseDTO;
import com.postech.execution.application.dto.FinalizarExecucaoRequestDTO;
import com.postech.execution.application.dto.NotasRequestDTO;
import com.postech.execution.application.usecase.ExecucaoServiceUsecase;
import com.postech.execution.domain.model.StatusExecucao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecucaoControllerApiTest {

    @Mock
    private ExecucaoServiceUsecase execucaoService;

    @InjectMocks
    private ExecucaoControllerApi controller;

    @Test
    void shouldListQueue() {
        UUID id = UUID.randomUUID();
        when(execucaoService.listarFila()).thenReturn(List.of(sample(id, StatusExecucao.EM_FILA, 1)));

        ResponseEntity<List<ExecucaoResponseDTO>> response = controller.listarFila();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(StatusExecucao.EM_FILA, response.getBody().get(0).status());
    }

    @Test
    void shouldStartDiagnosis() {
        UUID id = UUID.randomUUID();
        when(execucaoService.iniciarDiagnostico(id, "notes"))
                .thenReturn(sample(id, StatusExecucao.EM_DIAGNOSTICO, null));

        ResponseEntity<ExecucaoResponseDTO> response =
                controller.iniciarDiagnostico(id, new NotasRequestDTO("notes"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(StatusExecucao.EM_DIAGNOSTICO, response.getBody().status());
        verify(execucaoService).iniciarDiagnostico(id, "notes");
    }

    @Test
    void shouldFinalize() {
        UUID id = UUID.randomUUID();
        when(execucaoService.finalizar(id, "done"))
                .thenReturn(sample(id, StatusExecucao.FINALIZADA, null));

        ResponseEntity<ExecucaoResponseDTO> response =
                controller.finalizar(id, new FinalizarExecucaoRequestDTO("done"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(StatusExecucao.FINALIZADA, response.getBody().status());
    }

    private ExecucaoResponseDTO sample(UUID id, StatusExecucao status, Integer posicao) {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        return new ExecucaoResponseDTO(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                status,
                posicao,
                null,
                null,
                now,
                now,
                status == StatusExecucao.FINALIZADA ? now : null
        );
    }
}
