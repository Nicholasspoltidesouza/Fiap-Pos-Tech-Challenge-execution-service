package com.postech.execution.infrastructure.persistence.entity;

import com.postech.execution.domain.model.StatusExecucao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "execucao_trabalho")
public class ExecucaoTrabalhoEntity {

    @Id
    private UUID id;

    @Column(name = "ordem_servico_id", nullable = false)
    private UUID ordemServicoId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StatusExecucao status;

    @Column(name = "posicao_fila")
    private Integer posicaoFila;

    @Column(name = "diagnostico_notas", length = 2000)
    private String diagnosticoNotas;

    @Column(name = "reparo_notas", length = 2000)
    private String reparoNotas;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "finalizado_em")
    private Instant finalizadoEm;
}
