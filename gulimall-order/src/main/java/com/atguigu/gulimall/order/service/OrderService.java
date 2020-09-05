package com.atguigu.gulimall.order.service;

import com.atguigu.common.to.mq.SeckillOrderTO;
import com.atguigu.gulimall.order.vo.OrderConfirmVO;
import com.atguigu.gulimall.order.vo.OrderSubmitVO;
import com.atguigu.gulimall.order.vo.PayAsyncVo;
import com.atguigu.gulimall.order.vo.PayVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 18:53:59
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVO confirmOrder();

    OrderEntity submitOrder(OrderSubmitVO orderSubmit);

    OrderEntity getOrderByOrderSn(String orderSn);

    PayVO getOrderPay(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);

    String handlePayResult(PayAsyncVo payAsyncVo);

    void createSeckillOrder(SeckillOrderTO order);
}

