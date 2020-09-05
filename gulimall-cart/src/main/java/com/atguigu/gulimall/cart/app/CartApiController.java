package com.atguigu.gulimall.cart.app;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/cart/api")
@RestController
public class CartApiController {

    @Autowired
    private CartService cartService;

    @GetMapping("/getCurrentUserCartItems")
    public R getCurrentUserCartItems() {
        List<CartItemVO> cartItems = cartService.getCurrentUserCartItems();
        return R.ok().put("data", cartItems);
    }
}
