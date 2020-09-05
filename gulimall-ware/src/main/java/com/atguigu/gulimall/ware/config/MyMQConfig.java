package com.atguigu.gulimall.ware.config;

import com.google.common.collect.Maps;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MyMQConfig {

    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, true);
    }

    @Bean
    public Binding stockLockedBinding() {
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "stock.locked", Maps.newHashMap());
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("stock.release.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "stock.release.#", Maps.newHashMap());
    }

    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 120000);
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue stockReleaseQueue() {
        return new Queue("stock.release.queue", true, false, false);
    }

}
