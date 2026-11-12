package com.postech.execution.application.usecase;

import com.postech.execution.application.dto.ExecucaoResponseDTO;
import com.postech.execution.application.mapper.ExecucaoTrabalhoMapper;
import com.postech.execution.application.messaging.ExecutionEventPublisher;
import com.postech.execution.domain.exception.ExecucaoJaExisteException;
import com.postech.execution.domain.exception.ExecucaoNaoEncontradaException;
import com.postech.execution.domain.model.ExecucaoTrabalho;
import com.postech.execution.domain.model.StatusExecucao;
import com.postech.execution.infrastructure.persistence.entity.ExecucaoTrabalhoEntity;
import com.postech.execution.infrastructure.persistence.repository.ExecucaoTrabalhoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ExecucaoServiceUsecaseImpl implements ExecucaoServiceUsecase {

    private static final List<StatusExecucao> ACTIVE_STATUSES = List.of(
            StatusExecucao.EM_FILA,
            StatusExecucao.EM_DIAGNOSTICO,
            StatusExecucao.EM_REPARO
    );

    private final ExecucaoTrabalhoJpaRepository repository;
    private final ExecucaoTrabalhoMapper mapper;
    private final ExecutionEventPublisher eventPublisher;

    public ExecucaoServiceUsecaseImpl(
            ExecucaoTrabalhoJpaRepository repository,
            ExecucaoTrabalhoMapper mapper,
            ExecutionEventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ExecucaoResponseDTO iniciarExecucao(UUID ordemServicoId, UUID clienteId) {
        if (repository.existsByOrdemServicoIdAndStatusIn(ordemServicoId, ACTIVE_STATUSES)) {
            throw new ExecucaoJaExisteException(ordemServicoId);
        }

        Integer maxPosicao = repository.findMaxPosicaoFila();
        int proximaPosicao = (maxPosicao == null ? 0 : maxPosicao) + 1;

        ExecucaoTrabalho execucao = ExecucaoTrabalho.enfileirar(ordemServicoId, clienteId, proximaPosicao);
        ExecucaoTrabalhoEntity saved = repository.save(mapper.toEntity(execucao));
        ExecucaoTrabalho persisted = mapper.toDomain(saved);

        eventPublisher.publishExecucaoIniciada(persisted);
        return mapper.toResponse(persisted);
    }

    @Override
    public ExecucaoResponseDTO iniciarDiagnostico(UUID id, String notas) {
        ExecucaoTrabalho execucao = findDomain(id);
        execucao.iniciarDiagnostico(notas);
        return persistAndMap(execucao);
    }

    @Override
    public ExecucaoResponseDTO iniciarReparo(UUID id, String notas) {
        ExecucaoTrabalho execucao = findDomain(id);
        execucao.iniciarReparo(notas);
        return persistAndMap(execucao);
    }

    @Override
    public ExecucaoResponseDTO finalizar(UUID id, String notas) {
        ExecucaoTrabalho execucao = findDomain(id);
        execucao.finalizar(notas);
        ExecucaoResponseDTO response = persistAndMap(execucao);
        eventPublisher.publishExecucaoFinalizada(execucao);
        return response;
    }

    @Override
    public ExecucaoResponseDTO compensar(UUID ordemServicoId) {
        ExecucaoTrabalhoEntity entity = repository
                .findFirstByOrdemServicoIdOrderByCreatedAtDesc(ordemServicoId)
                .orElseThrow(() -> new ExecucaoNaoEncontradaException(
                        "No execution found for service order: " + ordemServicoId));

        ExecucaoTrabalho execucao = mapper.toDomain(entity);
        if (execucao.isTerminal()) {
            return mapper.toResponse(execucao);
        }

        execucao.cancelar();
        return persistAndMap(execucao);
    }

    @Override
    @Transactional(readOnly = true)
    public ExecucaoResponseDTO buscarPorId(UUID id) {
        return mapper.toResponse(findDomain(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ExecucaoResponseDTO buscarPorOrdemServico(UUID ordemServicoId) {
        return repository.findFirstByOrdemServicoIdOrderByCreatedAtDesc(ordemServicoId)
                .map(mapper::toDomain)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ExecucaoNaoEncontradaException(
                        "No execution found for service order: " + ordemServicoId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExecucaoResponseDTO> listarFila() {
        return repository.findByStatusOrderByPosicaoFilaAscCreatedAtAsc(StatusExecucao.EM_FILA)
                .stream()
                .map(mapper::toDomain)
                .map(mapper::toResponse)
                .toList();
    }

    private ExecucaoTrabalho findDomain(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ExecucaoNaoEncontradaException(id));
    }

    private ExecucaoResponseDTO persistAndMap(ExecucaoTrabalho execucao) {
        ExecucaoTrabalhoEntity entity = repository.findById(execucao.getId())
                .orElseGet(() -> mapper.toEntity(execucao));
        mapper.updateEntity(entity, execucao);
        ExecucaoTrabalhoEntity saved = repository.save(entity);
        return mapper.toResponse(mapper.toDomain(saved));
    }
}
