package com.atguigu.gulimall.product.app;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.enums.SpuStatusEnum;
import com.atguigu.gulimall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * spu信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-06-14 19:09:41
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:spuinfo:list")
    public R listByCoundtion(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageByCoundtion(params);

        return R.ok().put("page", page);
    }

    /**
     * 商品上架
     */
    @RequestMapping("/{spuId}/up")
    //@RequiresPermissions("ware:spuinfo:list")
    public R prdouctUp(@PathVariable(value = "spuId") Long spuId){
        spuInfoService.prdouctUp(spuId);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity SpuInfo = spuInfoService.getById(id);

        return R.ok().put("SpuInfo", SpuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:spuinfo:save")
    public R save(@RequestBody SpuInfoEntity SpuInfo){
        spuInfoService.saveSpuInfo(SpuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity SpuInfo){
        spuInfoService.updateById(SpuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
        spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @GetMapping("/getSpuInfo")
    public R getSpuInfo(@RequestParam("skuId") Long skuId) {
        SpuInfoEntity spuInfo = spuInfoService.getSpuInfo(skuId);
        return R.ok().put("data", spuInfo);
    }
}
