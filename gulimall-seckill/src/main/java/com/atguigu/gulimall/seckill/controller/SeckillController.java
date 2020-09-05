package com.atguigu.gulimall.seckill.controller;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/currentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTO> data =  seckillService.getCurrentSeckillSkus();
        return R.ok().put("data", data);
    }

    @GetMapping("/{skuId}")
    @ResponseBody
    public R getSkuSeckillInfo(@PathVariable Long skuId) {
        SeckillSkuRedisTO data = seckillService.getSkuSeckillInfo(skuId);
        return R.ok().put("data", data);
    }

    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {
        String orderSn = seckillService.seckill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }

}
