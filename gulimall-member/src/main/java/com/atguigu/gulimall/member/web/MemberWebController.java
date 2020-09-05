package com.atguigu.gulimall.member.web;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.feign.OrderFeignService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/web")
public class MemberWebController {

    @Autowired
    private OrderFeignService orderFeignService;

    @GetMapping("/orderList.html")
    public String orderList(@RequestParam(value = "pageNum", defaultValue = "1") String pageNum, Model model) {
        Map<String, Object> page = Maps.newHashMap();
        page.put("page", pageNum);
        R r = orderFeignService.listWithItem(page);
        model.addAttribute("orders", r.get("data"));
        return "orderList";
    }
}
