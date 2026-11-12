package com.postech.execution.application.mapper;

import com.postech.execution.application.dto.ExecucaoResponseDTO;
import com.postech.execution.domain.model.ExecucaoTrabalho;
import com.postech.execution.infrastructure.persistence.entity.ExecucaoTrabalhoEntity;
import org.springframework.stereotype.Component;

@Component
public class ExecucaoTrabalhoMapper {

    public ExecucaoTrabalho toDomain(ExecucaoTrabalhoEntity entity) {
        return ExecucaoTrabalho.rehydrate(
                entity.getId(),
                entity.getOrdemServicoId(),
                entity.getClienteId(),
                entity.getStatus(),
                entity.getPosicaoFila(),
                entity.getDiagnosticoNotas(),
                entity.getReparoNotas(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getFinalizadoEm()
        );
    }

    public ExecucaoTrabalhoEntity toEntity(ExecucaoTrabalho domain) {
        ExecucaoTrabalhoEntity entity = new ExecucaoTrabalhoEntity();
        entity.setId(domain.getId());
        entity.setOrdemServicoId(domain.getOrdemServicoId());
        entity.setClienteId(domain.getClienteId());
        entity.setStatus(domain.getStatus());
        entity.setPosicaoFila(domain.getPosicaoFila());
        entity.setDiagnosticoNotas(domain.getDiagnosticoNotas());
        entity.setReparoNotas(domain.getReparoNotas());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setFinalizadoEm(domain.getFinalizadoEm());
        return entity;
    }

    public void updateEntity(ExecucaoTrabalhoEntity entity, ExecucaoTrabalho domain) {
        entity.setStatus(domain.getStatus());
        entity.setPosicaoFila(domain.getPosicaoFila());
        entity.setDiagnosticoNotas(domain.getDiagnosticoNotas());
        entity.setReparoNotas(domain.getReparoNotas());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setFinalizadoEm(domain.getFinalizadoEm());
    }

    public ExecucaoResponseDTO toResponse(ExecucaoTrabalho domain) {
        return new ExecucaoResponseDTO(
                domain.getId(),
                domain.getOrdemServicoId(),
                domain.getClienteId(),
                domain.getStatus(),
                domain.getPosicaoFila(),
                domain.getDiagnosticoNotas(),
                domain.getReparoNotas(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getFinalizadoEm()
        );
    }
}
