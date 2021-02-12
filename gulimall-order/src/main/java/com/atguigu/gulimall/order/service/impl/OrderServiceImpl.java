package com.atguigu.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.UuidUtils;
import com.atguigu.common.to.UserInfoTO;
import com.atguigu.common.to.mq.SeckillOrderTO;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.order.constant.OrderConst;
import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.entity.PaymentInfoEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginUserInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTO;
import com.atguigu.gulimall.order.to.WareSkuLockTO;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVO confirmOrder() {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        UserInfoTO userInfoTO = LoginUserInterceptor.loginUser.get();
        orderConfirmVO.setIntegration(userInfoTO.getIntegration());

        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> orderConfirmVO.setMemberAddress(
                JSON.parseArray(JSON.toJSONString(memberFeignService.getAddress(userInfoTO.getId()).get("data")), MemberAddressVO.class)), threadPoolExecutor);

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> orderItemFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVO> orderItems = JSON.parseArray(JSON.toJSONString(cartFeignService.getCurrentUserCartItems().get("data")), OrderItemVO.class);
            orderConfirmVO.setOrderItems(orderItems);
            orderConfirmVO.setTotal(orderItems.stream().map(OrderItemVO::getTotalPrice).reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
            orderConfirmVO.setPayPrice(orderConfirmVO.getTotal());
            orderConfirmVO.setCount(orderItems.stream().map(OrderItemVO::getCount).reduce(Math::addExact).orElse(0));
        }, threadPoolExecutor);

        CompletableFuture<Void> hasStockFuture = orderItemFuture.thenRunAsync(() -> {
            List<Long> skuIds = orderConfirmVO.getOrderItems().stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
            R r = wareFeignService.getSkuStock(skuIds);
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(r.get("data")));
            orderConfirmVO.getOrderItems().forEach(e -> e.setHasStock(jsonObject.getInteger(String.valueOf(e.getSkuId())) > 0));
        }, threadPoolExecutor);

        CompletableFuture<Void> orderTokenFuture = CompletableFuture.runAsync(() -> {
            String token = UuidUtils.generateUuid();
            redissonClient.getBucket(OrderConst.REDIS_ORDER_COMMIT_TOKEN + userInfoTO.getId()).set(token, 30, TimeUnit.MINUTES);
            orderConfirmVO.setOrderToken(token);
        }, threadPoolExecutor);

        CompletableFuture.allOf(addressFuture, hasStockFuture, orderTokenFuture).join();
        return orderConfirmVO;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderEntity submitOrder(OrderSubmitVO orderSubmit) {
        UserInfoTO userInfoTO = LoginUserInterceptor.loginUser.get();

        Integer eval = redissonClient.getScript().eval(RScript.Mode.READ_WRITE, OrderConst.SCRIPT, RScript.ReturnType.INTEGER,
                Lists.newArrayList(OrderConst.REDIS_ORDER_COMMIT_TOKEN + userInfoTO.getId()), orderSubmit.getOrderToken());
        if (Objects.equals(eval, 0)) {
            return null;
        }
        OrderCreateTO createTO = createOrder(orderSubmit);
        if (!Objects.equals(createTO.getOrder().getPayAmount().setScale(2, BigDecimal.ROUND_HALF_UP), orderSubmit.getPayPrice())) {
            return null;
        }
        saveOrder(createTO);
        lockStock(createTO);
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create", createTO.getOrder());
        return createTO.getOrder();
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_sn", orderSn);
        return this.getOne(queryWrapper);
    }

    @Override
    public PayVO getOrderPay(String orderSn) {
        PayVO payVO = new PayVO();
        OrderEntity orderEntity = this.getOrderByOrderSn(orderSn);
        QueryWrapper<OrderItemEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_sn", orderSn);
        List<OrderItemEntity> orderItems = orderItemService.list(queryWrapper);
        orderItems.stream().findAny().ifPresent(e -> payVO.setSubject(e.getSpuName()));
        payVO.setOutTradeNo(orderSn);
        payVO.setTotalAmount(String.valueOf(orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP)));
        payVO.setBody(orderEntity.getNote());
        payVO.setTimeoutExpress(OrderConst.ORDER_TIMEOUT_EXPRESS);
        return payVO;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        UserInfoTO userInfoTO = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> iPage = new Query<OrderEntity>().getPage(params);
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", userInfoTO.getId());
        queryWrapper.orderByDesc("id");
        IPage<OrderEntity> page = this.page(iPage, queryWrapper);

        List<OrderEntity> orders = page.getRecords();
        List<String> orderSns = orders.stream().map(OrderEntity::getOrderSn).collect(Collectors.toList());
        QueryWrapper<OrderItemEntity> orderItemQueryWrapper = new QueryWrapper<>();
        orderItemQueryWrapper.in("order_sn", orderSns);
        List<OrderItemEntity> orderItems = orderItemService.list(orderItemQueryWrapper);

        Map<String, List<OrderItemEntity>> map = orderItems.stream().collect(Collectors.groupingBy(OrderItemEntity::getOrderSn));
        orders.stream().forEach(e -> e.setOrderItems(map.getOrDefault(e.getOrderSn(), Lists.newArrayList())));
        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo payAsyncVo) {
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setAlipayTradeNo(payAsyncVo.getTrade_no());
        paymentInfo.setOrderSn(payAsyncVo.getOut_trade_no());
        paymentInfo.setPaymentStatus(payAsyncVo.getTrade_status());
        paymentInfo.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(paymentInfo);

        if (Objects.equals("TRADE_SUCCESS", payAsyncVo.getTrade_status()) || Objects.equals("TRADE_FINISHED", payAsyncVo.getTrade_status())) {
            QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("order_sn", payAsyncVo.getTrade_no());
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setStatus(OrderStatusEnum.PAYED.getCode());
            this.update(orderEntity, queryWrapper);
        }
        return "success";
    }

    @Override
    public void createSeckillOrder(SeckillOrderTO order) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(order.getOrderSn());
        orderEntity.setMemberId(order.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal amount = order.getSeckillPrice().multiply(BigDecimal.valueOf(order.getNum()));
        orderEntity.setPayAmount(amount);
        orderEntity.setCreateTime(LocalDateTime.now());
        orderEntity.setModifyTime(LocalDateTime.now());
        this.save(orderEntity);

        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(order.getOrderSn());
        orderItemEntity.setRealAmount(amount);
        orderItemEntity.setSkuId(order.getSkuId());
        orderItemEntity.setSkuQuantity(order.getNum());
        orderItemService.save(orderItemEntity);
    }

    private void lockStock(OrderCreateTO createTO) {
        WareSkuLockTO wareSkuLockTO = new WareSkuLockTO();
        wareSkuLockTO.setOrderSn(createTO.getOrder().getOrderSn());
        List<OrderItemVO> orderItemS = createTO.getOrderItem().stream().map(e -> {
            OrderItemVO orderItem = new OrderItemVO();
            orderItem.setSkuId(e.getSkuId());
            orderItem.setCount(e.getSkuQuantity());
            orderItem.setSkuTitle(e.getSkuName());
            return orderItem;
        }).collect(Collectors.toList());
        wareSkuLockTO.setOrderItems(orderItemS);
        wareFeignService.orderLockStock(wareSkuLockTO);
    }

    private void saveOrder(OrderCreateTO createTO) {
        OrderEntity order = createTO.getOrder();
        order.setCreateTime(LocalDateTime.now());
        order.setModifyTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        this.save(order);
        orderItemService.saveBatch(createTO.getOrderItem());
    }

    private OrderCreateTO createOrder(OrderSubmitVO orderSubmit) {
        OrderCreateTO createTO = new OrderCreateTO();
        OrderEntity order = buildOrder(orderSubmit);
        createTO.setOrder(order);
        List<OrderItemEntity> orderItems = buildOrderItems(order.getOrderSn());
        createTO.setOrderItem(orderItems);
        computePrice(order, orderItems);
        return createTO;
    }

    private void computePrice(OrderEntity order, List<OrderItemEntity> orderItem) {
        BigDecimal totalAmount = orderItem.stream().map(OrderItemEntity::getRealAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount.add(order.getFreightAmount()));
        BigDecimal promotionAmount = orderItem.stream().map(OrderItemEntity::getPromotionAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        order.setPromotionAmount(promotionAmount);
        BigDecimal couponAmount = orderItem.stream().map(OrderItemEntity::getCouponAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        order.setCouponAmount(couponAmount);
        BigDecimal integrationAmount = orderItem.stream().map(OrderItemEntity::getIntegrationAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        order.setIntegrationAmount(integrationAmount);
        BigDecimal giftIntegration = orderItem.stream().map(OrderItemEntity::getGiftIntegration).map(BigDecimal::valueOf).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        order.setUseIntegration(giftIntegration.intValue());
        BigDecimal giftGrowth = orderItem.stream().map(OrderItemEntity::getGiftGrowth).map(BigDecimal::valueOf).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        order.setGrowth(giftGrowth.intValue());
    }

    private List<OrderItemEntity> buildOrderItems(String orderNum) {
        R r1 = cartFeignService.getCurrentUserCartItems();
        List<OrderItemVO> orderItems = JSON.parseArray(JSON.toJSONString(r1.get("data")), OrderItemVO.class);
        if (CollectionUtils.isNotEmpty(orderItems)) {
            return orderItems.stream().map(e -> {
                OrderItemEntity orderItem = this.buildOrderItems(e);
                orderItem.setOrderSn(orderNum);
                return orderItem;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private OrderItemEntity buildOrderItems(OrderItemVO orderItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        R r = productFeignService.getSpuInfo(orderItem.getSkuId());
        SpuInfoVO spuInfo = JSON.parseObject(JSON.toJSONString(r.get("data")), SpuInfoVO.class);
        orderItemEntity.setSpuId(spuInfo.getId());
        orderItemEntity.setSpuName(spuInfo.getSpuName());
        orderItemEntity.setSpuBrand(spuInfo.getBrandName());
        orderItemEntity.setCategoryId(spuInfo.getCatalogId());

        orderItemEntity.setSkuId(orderItem.getSkuId());
        orderItemEntity.setSkuName(orderItem.getSkuTitle());
        orderItemEntity.setSkuPic(orderItem.getSkuDefaultImg());
        orderItemEntity.setSkuPrice(orderItem.getPrice());
        orderItemEntity.setSkuAttrsVals(StringUtils.join(orderItem.getSkuAttrs(), ";"));
        orderItemEntity.setSkuQuantity(orderItem.getCount());

        orderItemEntity.setGiftGrowth(orderItem.getTotalPrice().intValue());
        orderItemEntity.setGiftIntegration(orderItem.getTotalPrice().intValue());

        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);
        orderItemEntity.setRealAmount(orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getCount()))
                .subtract(orderItemEntity.getPromotionAmount()).subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount()));

        return orderItemEntity;
    }

    private OrderEntity buildOrder(OrderSubmitVO orderSubmit) {
        UserInfoTO userInfoTO = LoginUserInterceptor.loginUser.get();
        OrderEntity order = new OrderEntity();
        order.setMemberId(userInfoTO.getId());
        order.setMemberUsername(userInfoTO.getName());
        order.setOrderSn(IdWorker.getTimeId());
        R r = wareFeignService.getFare(orderSubmit.getAddrId());
        MemberAddressVO addressVO = JSON.parseObject(JSON.toJSONString(r.get("data")), MemberAddressVO.class);
        order.setFreightAmount(addressVO.getFare());
        order.setReceiverCity(addressVO.getCity());
        order.setReceiverDetailAddress(addressVO.getDetailAddress());
        order.setReceiverName(addressVO.getName());
        order.setReceiverPhone(addressVO.getPhone());
        order.setReceiverPostCode(addressVO.getPostCode());
        order.setReceiverProvince(addressVO.getProvince());
        order.setReceiverRegion(addressVO.getRegion());
        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        order.setAutoConfirmDay(OrderConst.ORDER_AUTO_CONFIRM_DAY);
        order.setDeleteStatus(0);
        return order;
    }

}
