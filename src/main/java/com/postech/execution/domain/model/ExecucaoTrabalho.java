package com.postech.execution.domain.model;

import com.postech.execution.domain.exception.TransicaoStatusInvalidaException;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ExecucaoTrabalho {

    private static final Map<StatusExecucao, Set<StatusExecucao>> ALLOWED_TRANSITIONS =
            new EnumMap<>(StatusExecucao.class);

    static {
        ALLOWED_TRANSITIONS.put(StatusExecucao.EM_FILA,
                EnumSet.of(StatusExecucao.EM_DIAGNOSTICO, StatusExecucao.CANCELADA));
        ALLOWED_TRANSITIONS.put(StatusExecucao.EM_DIAGNOSTICO,
                EnumSet.of(StatusExecucao.EM_REPARO, StatusExecucao.CANCELADA));
        ALLOWED_TRANSITIONS.put(StatusExecucao.EM_REPARO,
                EnumSet.of(StatusExecucao.FINALIZADA, StatusExecucao.CANCELADA));
        ALLOWED_TRANSITIONS.put(StatusExecucao.FINALIZADA, EnumSet.noneOf(StatusExecucao.class));
        ALLOWED_TRANSITIONS.put(StatusExecucao.CANCELADA, EnumSet.noneOf(StatusExecucao.class));
    }

    private UUID id;
    private UUID ordemServicoId;
    private UUID clienteId;
    private StatusExecucao status;
    private Integer posicaoFila;
    private String diagnosticoNotas;
    private String reparoNotas;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant finalizadoEm;

    private ExecucaoTrabalho() {
    }

    public static ExecucaoTrabalho enfileirar(UUID ordemServicoId, UUID clienteId, Integer posicaoFila) {
        ExecucaoTrabalho execucao = new ExecucaoTrabalho();
        Instant now = Instant.now();
        execucao.id = UUID.randomUUID();
        execucao.ordemServicoId = ordemServicoId;
        execucao.clienteId = clienteId;
        execucao.status = StatusExecucao.EM_FILA;
        execucao.posicaoFila = posicaoFila;
        execucao.createdAt = now;
        execucao.updatedAt = now;
        return execucao;
    }

    public static ExecucaoTrabalho rehydrate(
            UUID id,
            UUID ordemServicoId,
            UUID clienteId,
            StatusExecucao status,
            Integer posicaoFila,
            String diagnosticoNotas,
            String reparoNotas,
            Instant createdAt,
            Instant updatedAt,
            Instant finalizadoEm) {
        ExecucaoTrabalho execucao = new ExecucaoTrabalho();
        execucao.id = id;
        execucao.ordemServicoId = ordemServicoId;
        execucao.clienteId = clienteId;
        execucao.status = status;
        execucao.posicaoFila = posicaoFila;
        execucao.diagnosticoNotas = diagnosticoNotas;
        execucao.reparoNotas = reparoNotas;
        execucao.createdAt = createdAt;
        execucao.updatedAt = updatedAt;
        execucao.finalizadoEm = finalizadoEm;
        return execucao;
    }

    public void iniciarDiagnostico(String notas) {
        transitionTo(StatusExecucao.EM_DIAGNOSTICO);
        this.diagnosticoNotas = notas;
        this.posicaoFila = null;
        touch();
    }

    public void iniciarReparo(String notas) {
        transitionTo(StatusExecucao.EM_REPARO);
        if (notas != null && !notas.isBlank()) {
            this.reparoNotas = notas;
        }
        touch();
    }

    public void finalizar(String notas) {
        transitionTo(StatusExecucao.FINALIZADA);
        if (notas != null && !notas.isBlank()) {
            this.reparoNotas = notas;
        }
        this.finalizadoEm = Instant.now();
        touch();
    }

    public void cancelar() {
        transitionTo(StatusExecucao.CANCELADA);
        this.posicaoFila = null;
        this.finalizadoEm = Instant.now();
        touch();
    }

    public boolean isTerminal() {
        return status == StatusExecucao.FINALIZADA || status == StatusExecucao.CANCELADA;
    }

    private void transitionTo(StatusExecucao novoStatus) {
        Set<StatusExecucao> allowed = ALLOWED_TRANSITIONS.getOrDefault(status, EnumSet.noneOf(StatusExecucao.class));
        if (!allowed.contains(novoStatus)) {
            throw new TransicaoStatusInvalidaException(status, novoStatus);
        }
        this.status = novoStatus;
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrdemServicoId() {
        return ordemServicoId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public StatusExecucao getStatus() {
        return status;
    }

    public Integer getPosicaoFila() {
        return posicaoFila;
    }

    public String getDiagnosticoNotas() {
        return diagnosticoNotas;
    }

    public String getReparoNotas() {
        return reparoNotas;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getFinalizadoEm() {
        return finalizadoEm;
    }
}
