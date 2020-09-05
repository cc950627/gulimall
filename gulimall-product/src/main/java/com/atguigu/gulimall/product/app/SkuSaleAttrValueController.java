package com.atguigu.gulimall.product.app;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.atguigu.gulimall.product.service.SkuSaleAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * sku销售属性&值
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-06-14 19:09:41
 */
@RestController
@RequestMapping("product/skusaleattrvalue")
public class SkuSaleAttrValueController {
    @Autowired
    private SkuSaleAttrValueService SkuSaleAttrValueService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:skusaleattrvalue:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = SkuSaleAttrValueService.queryPage(params);

        return R.ok().put("page", page);
    }

    @RequestMapping("/stringlist/{skuId}")
    public R stringlist(@PathVariable("skuId") Long skuId){
        List<String> data =  SkuSaleAttrValueService.stringlist(skuId);
        return R.ok().put("data", data);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:skusaleattrvalue:info")
    public R info(@PathVariable("id") Long id){
		SkuSaleAttrValueEntity SkuSaleAttrValue = SkuSaleAttrValueService.getById(id);

        return R.ok().put("SkuSaleAttrValue", SkuSaleAttrValue);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:skusaleattrvalue:save")
    public R save(@RequestBody SkuSaleAttrValueEntity SkuSaleAttrValue){
		SkuSaleAttrValueService.save(SkuSaleAttrValue);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:skusaleattrvalue:update")
    public R update(@RequestBody SkuSaleAttrValueEntity SkuSaleAttrValue){
		SkuSaleAttrValueService.updateById(SkuSaleAttrValue);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:skusaleattrvalue:delete")
    public R delete(@RequestBody Long[] ids){
		SkuSaleAttrValueService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
