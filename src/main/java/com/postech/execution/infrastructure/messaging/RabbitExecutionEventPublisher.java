package com.postech.execution.infrastructure.messaging;

import com.postech.execution.application.messaging.EventEnvelope;
import com.postech.execution.application.messaging.ExecutionEventPublisher;
import com.postech.execution.domain.model.ExecucaoTrabalho;
import com.postech.execution.infrastructure.config.MessagingProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RabbitExecutionEventPublisher implements ExecutionEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;

    public RabbitExecutionEventPublisher(RabbitTemplate rabbitTemplate, MessagingProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publishExecucaoIniciada(ExecucaoTrabalho execucao) {
        publish(
                properties.routingKeys().execucaoIniciada(),
                "ExecucaoIniciada",
                execucao,
                Map.of(
                        "execucaoId", execucao.getId().toString(),
                        "status", execucao.getStatus().name(),
                        "posicaoFila", execucao.getPosicaoFila()
                )
        );
    }

    @Override
    public void publishExecucaoFinalizada(ExecucaoTrabalho execucao) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("execucaoId", execucao.getId().toString());
        payload.put("status", execucao.getStatus().name());
        if (execucao.getFinalizadoEm() != null) {
            payload.put("finalizadoEm", execucao.getFinalizadoEm().toString());
        }
        publish(properties.routingKeys().execucaoFinalizada(), "ExecucaoFinalizada", execucao, payload);
    }

    private void publish(
            String routingKey,
            String eventType,
            ExecucaoTrabalho execucao,
            Map<String, Object> payload) {
        EventEnvelope envelope = EventEnvelope.of(
                eventType,
                execucao.getOrdemServicoId(),
                execucao.getClienteId(),
                payload
        );
        rabbitTemplate.convertAndSend(properties.exchange(), routingKey, envelope);
    }
}
