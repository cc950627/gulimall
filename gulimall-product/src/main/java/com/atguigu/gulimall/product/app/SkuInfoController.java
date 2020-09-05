package com.atguigu.gulimall.product.app;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.query.ProductQuery;
import com.atguigu.gulimall.product.service.SkuInfoService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * sku信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 17:36:56
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}/getPrice")
    public R getPrice(@PathVariable("skuId") Long skuId) {
        BigDecimal price = skuInfoService.getPrice(skuId);
        return R.ok().put("data", price);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuInfoService.queryPageByCoundtion(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/listBySkuIds")
    //@RequiresPermissions("product:skuinfo:list")
    public R listBySkuIds(@RequestBody List<Long> skuIds){
        List<SkuInfoEntity> data = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(skuIds)) {
            data = skuInfoService.listByIds(skuIds);
        }
        return R.ok().put("data", data);
    }

    /**
     * 按条件查询
     */
    @RequestMapping("/listByCoundtion")
    //@RequiresPermissions("product:skuinfo:list")
    public R listByCoundtion(@RequestBody ProductQuery params){
        List<SkuInfoEntity> data = skuInfoService.listByCoundtion(params);

        return R.ok().put("data", data);
    }

    /**
     * 获取sku对应的三级分类路径
     */
    @RequestMapping("/getCatelogIdsBySkuId")
    //@RequiresPermissions("product:skuinfo:list")
    public R getCatelogIdsBySkuId(@RequestParam(required = true) Long skuId){
        List<Long> data = skuInfoService.getCatelogIdsBySkuId(skuId);

        return R.ok().put("data", data);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info")
    public R info(@PathVariable("skuId") Long skuId){
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("data", skuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.save(skuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
