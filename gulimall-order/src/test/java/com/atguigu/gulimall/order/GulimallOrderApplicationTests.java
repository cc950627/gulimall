package com.atguigu.gulimall.order;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    public void createExchange() {
        DirectExchange directExchange = new DirectExchange("hello-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        System.out.println("创建交换机成功");
    }

    @Test
    public void createQueue() {
        Map<String, Object> arguments = Maps.newHashMap();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 120000);
        Queue a = new Queue("stock.delay.queue", true, false, false, arguments);
        amqpAdmin.declareQueue(a);
        System.out.println("创建队列成功");
    }

    @Test
    public void createBidding() {
        Binding binding = new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange", "stock.release", null);
        amqpAdmin.declareBinding(binding);
        System.out.println("绑定队列成功");
    }

    @Test
    public void sendMessage() {
        rabbitTemplate.convertAndSend("hello-exchange", "hello", "helloword");
        System.out.println("消息发送完成");
    }

}
