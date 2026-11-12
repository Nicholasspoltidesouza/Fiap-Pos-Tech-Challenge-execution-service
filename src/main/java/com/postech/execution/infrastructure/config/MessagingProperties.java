package com.postech.execution.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oficina.messaging")
public record MessagingProperties(
        String exchange,
        Queues queues,
        RoutingKeys routingKeys
) {
    public record Queues(String execucaoIniciar, String compensarExecucao) {
    }

    public record RoutingKeys(
            String execucaoIniciar,
            String compensarExecucao,
            String execucaoIniciada,
            String execucaoFinalizada
    ) {
    }
}
