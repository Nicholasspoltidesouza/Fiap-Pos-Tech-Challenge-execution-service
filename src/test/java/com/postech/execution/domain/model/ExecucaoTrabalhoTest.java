package com.postech.execution.domain.model;

import com.postech.execution.domain.exception.TransicaoStatusInvalidaException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecucaoTrabalhoTest {

    @Test
    void shouldEnqueueInFila() {
        ExecucaoTrabalho execucao = ExecucaoTrabalho.enfileirar(UUID.randomUUID(), UUID.randomUUID(), 1);

        assertEquals(StatusExecucao.EM_FILA, execucao.getStatus());
        assertEquals(1, execucao.getPosicaoFila());
        assertNotNull(execucao.getId());
        assertNotNull(execucao.getCreatedAt());
    }

    @Test
    void shouldProgressThroughHappyPath() {
        ExecucaoTrabalho execucao = ExecucaoTrabalho.enfileirar(UUID.randomUUID(), UUID.randomUUID(), 2);

        execucao.iniciarDiagnostico("noise in engine");
        assertEquals(StatusExecucao.EM_DIAGNOSTICO, execucao.getStatus());
        assertNull(execucao.getPosicaoFila());

        execucao.iniciarReparo("replace belt");
        assertEquals(StatusExecucao.EM_REPARO, execucao.getStatus());

        execucao.finalizar("done");
        assertEquals(StatusExecucao.FINALIZADA, execucao.getStatus());
        assertNotNull(execucao.getFinalizadoEm());
        assertTrue(execucao.isTerminal());
    }

    @Test
    void shouldCancelFromQueue() {
        ExecucaoTrabalho execucao = ExecucaoTrabalho.enfileirar(UUID.randomUUID(), UUID.randomUUID(), 1);
        execucao.cancelar();

        assertEquals(StatusExecucao.CANCELADA, execucao.getStatus());
        assertNull(execucao.getPosicaoFila());
        assertTrue(execucao.isTerminal());
    }

    @Test
    void shouldRejectInvalidTransition() {
        ExecucaoTrabalho execucao = ExecucaoTrabalho.enfileirar(UUID.randomUUID(), UUID.randomUUID(), 1);

        assertThrows(TransicaoStatusInvalidaException.class, () -> execucao.finalizar("too soon"));
    }
}
