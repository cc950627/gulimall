package com.atguigu.gulimall.order.config;

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
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, true);
    }

    @Bean
    public Binding orderCreateBinding() {
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.create", Maps.newHashMap());
    }

    @Bean
    public Binding orderReleaseBinding() {
        return new Binding("order.release.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.release.#", Maps.newHashMap());
    }

    @Bean
    public Binding stockReleaseBinding() {
        return new Binding("order.release.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "stock.release.#", Maps.newHashMap());
    }

    @Bean
    public Binding orderSeckillBinding() {
        return new Binding("order.seckill.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "stock.seckill.#", Maps.newHashMap());
    }

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release");
        arguments.put("x-message-ttl", 60000);
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue orderReleaseQueue() {
        return new Queue("order.release.queue", true, false, false);
    }

    @Bean
    public Queue orderSeckillQueue() {
        return new Queue("order.seckill.queue", true, false, false);
    }

}
