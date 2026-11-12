package com.postech.execution.application.messaging;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope(
        UUID eventId,
        String eventType,
        Instant ocorridoEm,
        UUID ordemServicoId,
        UUID clienteId,
        Map<String, Object> payload
) {
    public static EventEnvelope of(
            String eventType,
            UUID ordemServicoId,
            UUID clienteId,
            Map<String, Object> payload) {
        return new EventEnvelope(
                UUID.randomUUID(),
                eventType,
                Instant.now(),
                ordemServicoId,
                clienteId,
                payload
        );
    }
}
