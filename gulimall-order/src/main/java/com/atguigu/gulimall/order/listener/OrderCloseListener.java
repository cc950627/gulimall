package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@Service
@RabbitListener(queues = "order.release.queue")
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitHandler
    public void closeOrder(OrderEntity order, Message message, Channel channel) throws IOException {
        OrderEntity orderEntity = orderService.getById(order.getId());
        if (Objects.isNull(orderEntity)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        if (!Objects.equals(OrderStatusEnum.CREATE_NEW.getCode(), order.getStatus())) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        orderEntity.setStatus(OrderStatusEnum.CANCLED.getCode());
        orderEntity.setModifyTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        orderService.updateById(orderEntity);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        rabbitTemplate.convertAndSend("order-event-exchange", "stock.release", order);
    }

}
