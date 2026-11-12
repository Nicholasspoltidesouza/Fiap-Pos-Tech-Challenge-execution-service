package com.postech.execution.infrastructure.messaging;

import com.postech.execution.application.messaging.EventEnvelope;
import com.postech.execution.application.usecase.ExecucaoServiceUsecase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExecutionCommandListener {

    private static final Logger log = LoggerFactory.getLogger(ExecutionCommandListener.class);

    private final ExecucaoServiceUsecase execucaoService;

    public ExecutionCommandListener(ExecucaoServiceUsecase execucaoService) {
        this.execucaoService = execucaoService;
    }

    @RabbitListener(queues = "${oficina.messaging.queues.execucao-iniciar}")
    public void onExecucaoIniciar(EventEnvelope event) {
        log.info("Received execucao.iniciar for OS {}", event.ordemServicoId());
        UUID clienteId = event.clienteId() != null
                ? event.clienteId()
                : extractUuid(event.payload(), "clienteId");
        execucaoService.iniciarExecucao(event.ordemServicoId(), clienteId);
    }

    @RabbitListener(queues = "${oficina.messaging.queues.compensar-execucao}")
    public void onCompensarExecucao(EventEnvelope event) {
        log.info("Received compensar.execucao for OS {}", event.ordemServicoId());
        execucaoService.compensar(event.ordemServicoId());
    }

    private UUID extractUuid(java.util.Map<String, Object> payload, String key) {
        if (payload == null || payload.get(key) == null) {
            throw new IllegalArgumentException("Missing required payload field: " + key);
        }
        return UUID.fromString(payload.get(key).toString());
    }
}
