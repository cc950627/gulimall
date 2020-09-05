package com.atguigu.gulimall.product.app;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.SpuImagesEntity;
import com.atguigu.gulimall.product.service.SpuImagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * spu图片
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-06-14 19:09:41
 */
@RestController
@RequestMapping("product/spuimages")
public class ImagesController {
    @Autowired
    private SpuImagesService SpuImagesService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:spuimages:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = SpuImagesService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:spuimages:info")
    public R info(@PathVariable("id") Long id){
		SpuImagesEntity SpuImages = SpuImagesService.getById(id);

        return R.ok().put("SpuImages", SpuImages);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:spuimages:save")
    public R save(@RequestBody SpuImagesEntity SpuImages){
		SpuImagesService.save(SpuImages);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:spuimages:update")
    public R update(@RequestBody SpuImagesEntity SpuImages){
		SpuImagesService.updateById(SpuImages);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:spuimages:delete")
    public R delete(@RequestBody Long[] ids){
		SpuImagesService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
