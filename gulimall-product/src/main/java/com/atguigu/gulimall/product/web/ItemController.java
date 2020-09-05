package com.atguigu.gulimall.product.web;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.service.SkuInfoService;
import com.atguigu.gulimall.product.vo.SkuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItemPage(@PathVariable(value = "skuId") Long skuId, Model model) {
        SkuItemVO skuItem = skuInfoService.skuItem(skuId);
        model.addAttribute("result", skuItem);
        return "item";
    }
}
