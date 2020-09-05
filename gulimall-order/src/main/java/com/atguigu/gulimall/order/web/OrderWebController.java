package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.OrderConfirmVO;
import com.atguigu.gulimall.order.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RequestMapping("/order")
@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/toTrade.html")
    public String toTrade(Model model) {
        OrderConfirmVO orderComfirmVO =  orderService.confirmOrder();
        model.addAttribute("order", orderComfirmVO);
        return "confirm";
    }

    @PostMapping("/submitOrder.html")
    public String submitOrder(@RequestBody OrderSubmitVO orderSubmit, Model model) {
        OrderEntity order = orderService.submitOrder(orderSubmit);
        if (Objects.isNull(order)) {
            return "redirect:http://order.gulimall.com/order/toTrade.html";
        }
        model.addAttribute("order", order);
        return "pay";
    }

}
