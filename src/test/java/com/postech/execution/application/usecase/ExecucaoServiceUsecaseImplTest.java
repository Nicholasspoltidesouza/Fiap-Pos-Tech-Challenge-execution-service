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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecucaoServiceUsecaseImplTest {

    @Mock
    private ExecucaoTrabalhoJpaRepository repository;

    @Mock
    private ExecutionEventPublisher eventPublisher;

    private ExecucaoServiceUsecaseImpl usecase;

    private final ExecucaoTrabalhoMapper mapper = new ExecucaoTrabalhoMapper();

    @BeforeEach
    void setUp() {
        usecase = new ExecucaoServiceUsecaseImpl(repository, mapper, eventPublisher);
    }

    @Test
    void shouldCreateJobAndPublishIniciada() {
        UUID ordemId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        when(repository.existsByOrdemServicoIdAndStatusIn(eq(ordemId), anyList())).thenReturn(false);
        when(repository.findMaxPosicaoFila()).thenReturn(3);
        when(repository.save(any(ExecucaoTrabalhoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecucaoResponseDTO response = usecase.iniciarExecucao(ordemId, clienteId);

        assertEquals(StatusExecucao.EM_FILA, response.status());
        assertEquals(4, response.posicaoFila());
        verify(eventPublisher).publishExecucaoIniciada(any(ExecucaoTrabalho.class));
    }

    @Test
    void shouldRejectDuplicateActiveJob() {
        UUID ordemId = UUID.randomUUID();
        when(repository.existsByOrdemServicoIdAndStatusIn(eq(ordemId), anyList())).thenReturn(true);

        assertThrows(ExecucaoJaExisteException.class,
                () -> usecase.iniciarExecucao(ordemId, UUID.randomUUID()));
        verify(eventPublisher, never()).publishExecucaoIniciada(any());
    }

    @Test
    void shouldFinalizeAndPublishFinalizada() {
        ExecucaoTrabalho execucao = ExecucaoTrabalho.enfileirar(UUID.randomUUID(), UUID.randomUUID(), 1);
        execucao.iniciarDiagnostico("ok");
        execucao.iniciarReparo("fixing");
        ExecucaoTrabalhoEntity entity = mapper.toEntity(execucao);

        when(repository.findById(execucao.getId())).thenReturn(Optional.of(entity));
        when(repository.save(any(ExecucaoTrabalhoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecucaoResponseDTO response = usecase.finalizar(execucao.getId(), "completed");

        assertEquals(StatusExecucao.FINALIZADA, response.status());
        ArgumentCaptor<ExecucaoTrabalho> captor = ArgumentCaptor.forClass(ExecucaoTrabalho.class);
        verify(eventPublisher).publishExecucaoFinalizada(captor.capture());
        assertEquals(StatusExecucao.FINALIZADA, captor.getValue().getStatus());
    }

    @Test
    void shouldCompensateByCancellingActiveJob() {
        UUID ordemId = UUID.randomUUID();
        ExecucaoTrabalho execucao = ExecucaoTrabalho.enfileirar(ordemId, UUID.randomUUID(), 1);
        ExecucaoTrabalhoEntity entity = mapper.toEntity(execucao);

        when(repository.findFirstByOrdemServicoIdOrderByCreatedAtDesc(ordemId)).thenReturn(Optional.of(entity));
        when(repository.findById(execucao.getId())).thenReturn(Optional.of(entity));
        when(repository.save(any(ExecucaoTrabalhoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExecucaoResponseDTO response = usecase.compensar(ordemId);

        assertEquals(StatusExecucao.CANCELADA, response.status());
    }

    @Test
    void shouldListQueueOrdered() {
        ExecucaoTrabalho first = ExecucaoTrabalho.enfileirar(UUID.randomUUID(), UUID.randomUUID(), 1);
        ExecucaoTrabalho second = ExecucaoTrabalho.enfileirar(UUID.randomUUID(), UUID.randomUUID(), 2);
        when(repository.findByStatusOrderByPosicaoFilaAscCreatedAtAsc(StatusExecucao.EM_FILA))
                .thenReturn(List.of(mapper.toEntity(first), mapper.toEntity(second)));

        List<ExecucaoResponseDTO> fila = usecase.listarFila();

        assertEquals(2, fila.size());
        assertEquals(1, fila.get(0).posicaoFila());
        assertEquals(2, fila.get(1).posicaoFila());
    }

    @Test
    void shouldThrowWhenJobNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ExecucaoNaoEncontradaException.class, () -> usecase.buscarPorId(id));
    }

    @Test
    void shouldStartDiagnosisAndRepair() {
        ExecucaoTrabalho execucao = ExecucaoTrabalho.enfileirar(UUID.randomUUID(), UUID.randomUUID(), 1);
        ExecucaoTrabalhoEntity entity = mapper.toEntity(execucao);

        when(repository.findById(execucao.getId())).thenReturn(Optional.of(entity));
        when(repository.save(any(ExecucaoTrabalhoEntity.class))).thenAnswer(invocation -> {
            ExecucaoTrabalhoEntity saved = invocation.getArgument(0);
            saved.setUpdatedAt(Instant.now());
            return saved;
        });

        ExecucaoResponseDTO diagnostico = usecase.iniciarDiagnostico(execucao.getId(), "check brakes");
        assertEquals(StatusExecucao.EM_DIAGNOSTICO, diagnostico.status());

        entity.setStatus(StatusExecucao.EM_DIAGNOSTICO);
        ExecucaoResponseDTO reparo = usecase.iniciarReparo(execucao.getId(), "pads");
        assertEquals(StatusExecucao.EM_REPARO, reparo.status());
    }
}
