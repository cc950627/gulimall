package com.atguigu.gulimall.ware.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.mq.OrderTO;
import com.atguigu.common.to.mq.StockLockDetailTO;
import com.atguigu.common.to.mq.StockLockTO;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.constant.WareLockStatusEnum;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.feign.OrderFeignService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RabbitListener(queues = "stock.release.queue")
public class StockReleaseQueueListener {

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Transactional
    @RabbitHandler
    public void stockReleaseHandler(StockLockTO stockLockTO, Message message, Channel channel) throws IOException {
        WareOrderTaskEntity task = wareOrderTaskService.getById(stockLockTO.getId());
        if (Objects.isNull(task)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        List<Long> taskDetailIds = stockLockTO.getStockLockDetails().stream().map(StockLockDetailTO::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(taskDetailIds)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.listByIds(taskDetailIds);
        if (CollectionUtils.isEmpty(detailEntities)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        List<WareOrderTaskDetailEntity> lockedTaskDetails = detailEntities.stream().filter(e
                -> Objects.equals(WareLockStatusEnum.ISLOCK.getStatus(), e.getLockStatus())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(lockedTaskDetails)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        R r = orderFeignService.getOrder(task.getOrderSn());
        if (Objects.equals(0, r.getCode())) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            return;
        }
        if (Objects.nonNull(r.get("data"))) {
            OrderVO orderVO = JSON.parseObject(JSON.toJSONString(r.get("data")), OrderVO.class);
            if (!Objects.equals(orderVO.getStatus(), 4)) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
        }
        unLockedSotck(lockedTaskDetails);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @Transactional
    @RabbitHandler
    public void orderCloseReleaseHandler(OrderTO orderTO, Message message, Channel channel) throws IOException {
        QueryWrapper<WareOrderTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_sn", orderTO.getOrderSn());
        WareOrderTaskEntity orderTask = wareOrderTaskService.getOne(queryWrapper);

        QueryWrapper<WareOrderTaskDetailEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", orderTask.getId());
        wrapper.eq("lock_status", WareLockStatusEnum.ISLOCK.getStatus());
        List<WareOrderTaskDetailEntity> lockedTaskDetails = wareOrderTaskDetailService.list(wrapper);
        if (CollectionUtils.isNotEmpty(lockedTaskDetails)) {
            unLockedSotck(lockedTaskDetails);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    private void unLockedSotck(List<WareOrderTaskDetailEntity> lockedTaskDetails) {
        QueryWrapper<WareSkuEntity> wareSkuQueryWrapper = new QueryWrapper<>();
        lockedTaskDetails.stream().forEach(e -> wareSkuQueryWrapper.or(o -> o.eq("sku_id", e.getSkuId()).eq("ware_id", e.getWareId())));
        wareSkuQueryWrapper.last("for update");
        List<WareSkuEntity> wareSkuEntities = wareSkuService.list(wareSkuQueryWrapper);
        wareSkuEntities.stream().forEach(e -> lockedTaskDetails.stream().filter(
                o -> Objects.equals(o.getSkuId(), e.getSkuId()) && Objects.equals(o.getWareId(), e.getWareId())).findAny()
                .ifPresent(o -> e.setStockLocked(e.getStockLocked() - o.getSkuNum())));
        wareSkuService.updateBatchById(wareSkuEntities);

        lockedTaskDetails.stream().forEach(e -> e.setLockStatus(WareLockStatusEnum.ISUNLOCK.getStatus()));
        wareOrderTaskDetailService.updateBatchById(lockedTaskDetails);
    }

}
