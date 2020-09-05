package com.atguigu.gulimall.order.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMessageConverterConfig {

    private RabbitTemplate rabbitTemplate;

    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        this.rabbitTemplate = rabbitTemplate;
        initRabbitTemplate();
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    public void initRabbitTemplate() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            System.out.println("correlationData" + correlationData);
            System.out.println("ack" + ack);
            System.out.println("cause" + cause);
        });

        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            System.out.println("message" + message);
            System.out.println("replyCode" + replyCode);
            System.out.println("replyText" + replyText);
            System.out.println("routingKey" + routingKey);
        });

    }
}
