package com.postech.execution.infrastructure.persistence.repository;

import com.postech.execution.domain.model.StatusExecucao;
import com.postech.execution.infrastructure.persistence.entity.ExecucaoTrabalhoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecucaoTrabalhoJpaRepository extends JpaRepository<ExecucaoTrabalhoEntity, UUID> {

    List<ExecucaoTrabalhoEntity> findByStatusOrderByPosicaoFilaAscCreatedAtAsc(StatusExecucao status);

    Optional<ExecucaoTrabalhoEntity> findFirstByOrdemServicoIdOrderByCreatedAtDesc(UUID ordemServicoId);

    boolean existsByOrdemServicoIdAndStatusIn(UUID ordemServicoId, List<StatusExecucao> statuses);

    @Query("select coalesce(max(e.posicaoFila), 0) from ExecucaoTrabalhoEntity e where e.status = com.postech.execution.domain.model.StatusExecucao.EM_FILA")
    Integer findMaxPosicaoFila();
}
