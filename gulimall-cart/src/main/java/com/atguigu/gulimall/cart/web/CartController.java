package com.atguigu.gulimall.cart.web;

import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.vo.CartItemVO;
import com.atguigu.gulimall.cart.vo.CartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RequestMapping("/cart")
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/list.html")
    public String cartListPage(Model model){
        CartVO cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    @GetMapping("/addCart")
    public String addCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes ra){
        cartService.addCart(skuId, num);
        ra.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/cart/success.html";
    }

    @GetMapping("/success.html")
    public String successPage(@RequestParam("skuId") Long skuId, Model model) {
        CartItemVO cartItemVO = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItemVO);
        return "success";
    }

    @RequestMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Boolean check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com/cart/list.html";
    }

    @RequestMapping("/updateItemNum")
    public String updateItemNum(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.updateItemNum(skuId, num);
        return "redirect:http://cart.gulimall.com/cart/list.html";
    }

    @RequestMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart/list.html";
    }

}
