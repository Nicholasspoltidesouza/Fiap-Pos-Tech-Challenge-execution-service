package com.postech.execution.bdd;

import com.postech.execution.application.dto.ExecucaoResponseDTO;
import com.postech.execution.application.mapper.ExecucaoTrabalhoMapper;
import com.postech.execution.application.messaging.ExecutionEventPublisher;
import com.postech.execution.application.usecase.ExecucaoServiceUsecaseImpl;
import com.postech.execution.domain.model.ExecucaoTrabalho;
import com.postech.execution.domain.model.StatusExecucao;
import com.postech.execution.infrastructure.persistence.entity.ExecucaoTrabalhoEntity;
import com.postech.execution.infrastructure.persistence.repository.ExecucaoTrabalhoJpaRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExecucaoFlowSteps {

    private final ExecucaoTrabalhoMapper mapper = new ExecucaoTrabalhoMapper();
    private ConcurrentMap<UUID, ExecucaoTrabalhoEntity> store;
    private RecordingPublisher publisher;
    private ExecucaoServiceUsecaseImpl usecase;
    private UUID ordemServicoId;
    private UUID clienteId;
    private ExecucaoResponseDTO current;

    @Before
    public void setUp() {
        store = new ConcurrentHashMap<>();
        publisher = new RecordingPublisher();

        ExecucaoTrabalhoJpaRepository repository = mock(ExecucaoTrabalhoJpaRepository.class);

        when(repository.existsByOrdemServicoIdAndStatusIn(any(UUID.class), anyList()))
                .thenAnswer(invocation -> {
                    UUID ordemId = invocation.getArgument(0);
                    List<StatusExecucao> statuses = invocation.getArgument(1);
                    return store.values().stream()
                            .anyMatch(entity -> entity.getOrdemServicoId().equals(ordemId)
                                    && statuses.contains(entity.getStatus()));
                });

        when(repository.findMaxPosicaoFila()).thenAnswer(invocation -> store.values().stream()
                .filter(entity -> entity.getStatus() == StatusExecucao.EM_FILA)
                .map(ExecucaoTrabalhoEntity::getPosicaoFila)
                .filter(posicao -> posicao != null)
                .max(Integer::compareTo)
                .orElse(0));

        when(repository.save(any(ExecucaoTrabalhoEntity.class))).thenAnswer(invocation -> {
            ExecucaoTrabalhoEntity entity = invocation.getArgument(0);
            store.put(entity.getId(), entity);
            return entity;
        });

        when(repository.findById(any(UUID.class))).thenAnswer(invocation ->
                Optional.ofNullable(store.get(invocation.getArgument(0))));

        when(repository.findFirstByOrdemServicoIdOrderByCreatedAtDesc(any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID ordemId = invocation.getArgument(0);
                    return store.values().stream()
                            .filter(entity -> entity.getOrdemServicoId().equals(ordemId))
                            .findFirst();
                });

        when(repository.findByStatusOrderByPosicaoFilaAscCreatedAtAsc(eq(StatusExecucao.EM_FILA)))
                .thenAnswer(invocation -> store.values().stream()
                        .filter(entity -> entity.getStatus() == StatusExecucao.EM_FILA)
                        .toList());

        usecase = new ExecucaoServiceUsecaseImpl(repository, mapper, publisher);
        ordemServicoId = null;
        clienteId = null;
        current = null;
    }

    @Given("a new service order ready for execution")
    public void aNewServiceOrderReadyForExecution() {
        ordemServicoId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
    }

    @When("the execution start command is processed")
    public void theExecutionStartCommandIsProcessed() {
        current = usecase.iniciarExecucao(ordemServicoId, clienteId);
    }

    @Then("the job should be in queue with status EM_FILA")
    public void theJobShouldBeInQueueWithStatusEmFila() {
        assertEquals(StatusExecucao.EM_FILA, current.status());
        assertTrue(current.posicaoFila() >= 1);
    }

    @Then("an ExecucaoIniciada event should have been published")
    public void anExecucaoIniciadaEventShouldHaveBeenPublished() {
        assertEquals(1, publisher.iniciadas.size());
    }

    @When("the operator starts diagnosis")
    public void theOperatorStartsDiagnosis() {
        current = usecase.iniciarDiagnostico(current.id(), "diagnosed");
    }

    @Then("the job status should be EM_DIAGNOSTICO")
    public void theJobStatusShouldBeEmDiagnostico() {
        assertEquals(StatusExecucao.EM_DIAGNOSTICO, current.status());
    }

    @When("the operator starts repair")
    public void theOperatorStartsRepair() {
        current = usecase.iniciarReparo(current.id(), "repairing");
    }

    @Then("the job status should be EM_REPARO")
    public void theJobStatusShouldBeEmReparo() {
        assertEquals(StatusExecucao.EM_REPARO, current.status());
    }

    @When("the operator finishes the job")
    public void theOperatorFinishesTheJob() {
        current = usecase.finalizar(current.id(), "done");
    }

    @Then("the job status should be FINALIZADA")
    public void theJobStatusShouldBeFinalizada() {
        assertEquals(StatusExecucao.FINALIZADA, current.status());
    }

    @Then("an ExecucaoFinalizada event should have been published")
    public void anExecucaoFinalizadaEventShouldHaveBeenPublished() {
        assertEquals(1, publisher.finalizadas.size());
    }

    @When("the compensation command is processed")
    public void theCompensationCommandIsProcessed() {
        current = usecase.compensar(ordemServicoId);
    }

    @Then("the job status should be CANCELADA")
    public void theJobStatusShouldBeCancelada() {
        assertEquals(StatusExecucao.CANCELADA, current.status());
    }

    private static final class RecordingPublisher implements ExecutionEventPublisher {
        private final List<ExecucaoTrabalho> iniciadas = new ArrayList<>();
        private final List<ExecucaoTrabalho> finalizadas = new ArrayList<>();

        @Override
        public void publishExecucaoIniciada(ExecucaoTrabalho execucao) {
            iniciadas.add(execucao);
        }

        @Override
        public void publishExecucaoFinalizada(ExecucaoTrabalho execucao) {
            finalizadas.add(execucao);
        }
    }
}
