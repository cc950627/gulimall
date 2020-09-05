package com.atguigu.gulimall.order;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
        Queue queue = new Queue("hello-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        System.out.println("创建队列成功");
    }

    @Test
    public void createBidding() {
        Binding binding = new Binding("hello-queue", Binding.DestinationType.QUEUE,
                "hello-exchange", "hello", null);
        amqpAdmin.declareBinding(binding);
        System.out.println("绑定队列成功");
    }

    @Test
    public void sendMessage() {
        rabbitTemplate.convertAndSend("hello-exchange", "hello", "helloword");
        System.out.println("消息发送完成");
    }

}
