package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.constant.CartConstant;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.to.CartUserInfoTO;
import com.atguigu.gulimall.cart.vo.CartItemVO;
import com.atguigu.gulimall.cart.vo.CartVO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public CartItemVO addCart(Long skuId, Integer num) {
        RMap<Long, CartItemVO> cart = listCartItem();
        Optional<CartItemVO> optional = Optional.ofNullable(cart.getAsync(skuId).join());
        optional.ifPresent(e -> {
            e.setCount(e.getCount() + num);
            e.setTotalPrice(e.getPrice().multiply(BigDecimal.valueOf(e.getCount())));
        });

        CartItemVO cartItemVO = optional.orElseGet(() -> {
            CompletableFuture<CartItemVO> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
                CartItemVO cartItem = JSON.parseObject(JSON.toJSONString(productFeignService.info(skuId).get("data")), CartItemVO.class);
                cartItem.setCount(num);
                cartItem.setCheck(true);
                cartItem.setTotalPrice(cartItem.getPrice().multiply(BigDecimal.valueOf(num)));
                return cartItem;
            }, threadPoolExecutor);

            CompletableFuture<List<String>> saleAttrFuture = CompletableFuture.supplyAsync(() -> JSON.parseArray(
                    JSON.toJSONString(productFeignService.stringlist(skuId).get("data")), String.class), threadPoolExecutor);
            CartItemVO cartItem = skuInfoFuture.join();
            cartItem.setSkuAttrs(saleAttrFuture.join());
            return cartItem;
        });
        cart.putAsync(skuId, cartItemVO);
        return cartItemVO;
    }

    @Override
    public CartItemVO getCartItem(Long skuId) {
        CompletableFuture<CartItemVO> cartItemFuture = CompletableFuture.supplyAsync(() -> listCartItem().getAsync(skuId).join(), threadPoolExecutor);
        CompletableFuture<R> priceFuture = CompletableFuture.supplyAsync(() -> productFeignService.getPrice(skuId), threadPoolExecutor);
        CartItemVO cartItemVO = cartItemFuture.join();
        cartItemVO.setPrice(new BigDecimal(String.valueOf(priceFuture.join().get("data"))));
        cartItemVO.setTotalPrice(cartItemVO.getPrice().multiply(BigDecimal.valueOf(cartItemVO.getCount())));
        return cartItemVO;
    }

    @Override
    public CartVO getCart() {
        CartVO cartVO = new CartVO();
        CartUserInfoTO cartUserInfoTO = CartInterceptor.threadLocal.get();
        RMap<Long, CartItemVO> offCart = redissonClient.getMap(CartConstant.REDIS_CART_PREFIX + cartUserInfoTO.getUserKey());
        if (Objects.isNull(cartUserInfoTO.getUserId())) {
            cartVO.setItems(Lists.newArrayList(offCart.values()));
        } else {
            RMap<Long, CartItemVO> onCart = redissonClient.getMap(CartConstant.REDIS_CART_PREFIX + cartUserInfoTO.getUserId());
            offCart.forEach((k, v) -> {
                RFuture<CartItemVO> cartItemVORFuture = onCart.putIfAbsentAsync(k, v);
                Optional.ofNullable(cartItemVORFuture.join()).ifPresent(e -> {
                    e.setCount(e.getCount() + v.getCount());
                    e.setTotalPrice(e.getTotalPrice().add(v.getTotalPrice()));
                    onCart.putAsync(e.getSkuId(), e);
                });
            });
            cartVO.setItems(Lists.newArrayList(onCart.values()));
        }
        cartVO.setCountType(cartVO.getItems().size());
        cartVO.setCountNum(cartVO.getItems().stream().mapToInt(CartItemVO::getCount).sum());
        cartVO.setTotalAmount(cartVO.getItems().stream().filter(CartItemVO::getCheck).map(CartItemVO::getTotalPrice)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO));
        cartVO.setReduce(BigDecimal.ZERO);
        return cartVO;
    }

    @Override
    public void checkItem(Long skuId, Boolean check) {
        RMap<Long, CartItemVO> cart = listCartItem();
        CartItemVO cartItemVO = cart.getAsync(skuId).join();
        cartItemVO.setCheck(check);
        cart.putAsync(skuId, cartItemVO);
    }

    @Override
    public void updateItemNum(Long skuId, Integer num) {
        RMap<Long, CartItemVO> cart = listCartItem();
        CartItemVO cartItemVO = cart.getAsync(skuId).join();
        cartItemVO.setCount(num);
        cartItemVO.setTotalPrice(cartItemVO.getPrice().multiply(BigDecimal.valueOf(num)));
        cart.putAsync(skuId, cartItemVO);
    }

    @Override
    public void deleteItem(Long skuId) {
        RMap<Long, CartItemVO> cart = listCartItem();
        cart.removeAsync(skuId);
    }

    @Override
    public List<CartItemVO> getCurrentUserCartItems() {
        RMap<Long, CartItemVO> cart = listCartItem();
        return cart.values().stream().filter(CartItemVO::getCheck).peek(e -> {
            e.setPrice(new BigDecimal(String.valueOf(productFeignService.getPrice(e.getSkuId()).get("data"))));
            e.setTotalPrice(e.getPrice().multiply(BigDecimal.valueOf(e.getCount())));
        }).collect(Collectors.toList());
    }

    private RMap<Long, CartItemVO> listCartItem() {
        CartUserInfoTO cartUserInfoTO = CartInterceptor.threadLocal.get();
        String cartKey = cartUserInfoTO.getUserKey();
        if (Objects.nonNull(cartUserInfoTO.getUserId())) {
            cartKey = String.valueOf(cartUserInfoTO.getUserId());
        }
        cartKey = CartConstant.REDIS_CART_PREFIX + cartKey;
        return redissonClient.getMap(cartKey);
    }
}
