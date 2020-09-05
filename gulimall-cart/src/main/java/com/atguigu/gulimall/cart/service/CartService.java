package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartItemVO;
import com.atguigu.gulimall.cart.vo.CartVO;

import java.util.List;

public interface CartService {

    CartItemVO addCart(Long skuId, Integer num);

    CartItemVO getCartItem(Long skuId);

    CartVO getCart();

    void checkItem(Long skuId, Boolean check);

    void updateItemNum(Long skuId, Integer num);

    void deleteItem(Long skuId);

    List<CartItemVO> getCurrentUserCartItems();
}
