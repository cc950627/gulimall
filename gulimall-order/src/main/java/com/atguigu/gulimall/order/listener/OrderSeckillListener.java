package com.atguigu.gulimall.order.listener;

import com.atguigu.common.to.mq.SeckillOrderTO;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "order.seckill.queue")
public class OrderSeckillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void SeckillOrder(SeckillOrderTO order, Message message, Channel channel) throws IOException {
        orderService.createSeckillOrder(order);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
