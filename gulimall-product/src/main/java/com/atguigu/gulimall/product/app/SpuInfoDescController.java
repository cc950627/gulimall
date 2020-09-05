package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.service.SpuInfoDescService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * spu信息介绍
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-06-14 19:09:41
 */
@RestController
@RequestMapping("product/spuinfodesc")
public class SpuInfoDescController {
    @Autowired
    private SpuInfoDescService SpuInfoDescService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:spuinfodesc:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = SpuInfoDescService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{spuId}")
    //@RequiresPermissions("ware:spuinfodesc:info")
    public R info(@PathVariable("spuId") Long spuId){
		SpuInfoDescEntity SpuInfoDesc = SpuInfoDescService.getById(spuId);

        return R.ok().put("SpuInfoDesc", SpuInfoDesc);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:spuinfodesc:save")
    public R save(@RequestBody SpuInfoDescEntity SpuInfoDesc){
		SpuInfoDescService.save(SpuInfoDesc);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:spuinfodesc:update")
    public R update(@RequestBody SpuInfoDescEntity SpuInfoDesc){
		SpuInfoDescService.updateById(SpuInfoDesc);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:spuinfodesc:delete")
    public R delete(@RequestBody Long[] spuIds){
		SpuInfoDescService.removeByIds(Arrays.asList(spuIds));

        return R.ok();
    }

}
