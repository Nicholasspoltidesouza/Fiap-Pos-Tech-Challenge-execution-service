package com.postech.execution.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MessagingProperties.class)
public class RabbitMqConfig {

    @Bean
    public TopicExchange oficinaEventsExchange(MessagingProperties properties) {
        return new TopicExchange(properties.exchange(), true, false);
    }

    @Bean
    public Queue execucaoIniciarQueue(MessagingProperties properties) {
        return new Queue(properties.queues().execucaoIniciar(), true);
    }

    @Bean
    public Queue compensarExecucaoQueue(MessagingProperties properties) {
        return new Queue(properties.queues().compensarExecucao(), true);
    }

    @Bean
    public Binding execucaoIniciarBinding(
            Queue execucaoIniciarQueue,
            TopicExchange oficinaEventsExchange,
            MessagingProperties properties) {
        return BindingBuilder
                .bind(execucaoIniciarQueue)
                .to(oficinaEventsExchange)
                .with(properties.routingKeys().execucaoIniciar());
    }

    @Bean
    public Binding compensarExecucaoBinding(
            Queue compensarExecucaoQueue,
            TopicExchange oficinaEventsExchange,
            MessagingProperties properties) {
        return BindingBuilder
                .bind(compensarExecucaoQueue)
                .to(oficinaEventsExchange)
                .with(properties.routingKeys().compensarExecucao());
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter);
        return factory;
    }
}
