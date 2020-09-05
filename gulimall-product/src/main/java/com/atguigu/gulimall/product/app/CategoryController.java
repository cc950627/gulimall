package com.atguigu.gulimall.product.app;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 商品三级分类
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 17:36:56
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 商品分类树
     */
    @RequestMapping("/list/tree")
    //@RequiresPermissions("product:category:list")
    public R list(){
        List<CategoryEntity> categoryTree = categoryService.listWithTreeRedisLock();
        return R.ok().put("data", categoryTree);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
		boolean result = categoryService.save(category);
        return R.ok().put("data", result);
    }

    /**
     * 修改
     */
    @RequestMapping("/update/sort")
    //@RequiresPermissions("product:category:update")
    public R updateSort(@RequestBody CategoryEntity[] category){
        boolean result = categoryService.updateBatchById(Lists.newArrayList(category));

        return R.ok().put("data", result);
    }

    /**
     * 批量修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
        boolean result = categoryService.updateDetailById(category);

        return R.ok().put("data", result);
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:category:delete")
    public R delete(@RequestBody List<Long> catIds){
        boolean result = categoryService.deteleMeunByIds(catIds);
        return R.ok().put("data", result);
    }

}
