package com.atguigu.gulimall.product.app;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.pig4cloud.plugin.idempotent.annotation.Idempotent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * 商品属性
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 17:36:56
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
    @Idempotent(key = "#params", expireTime = 3)
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/{type}/list/{catId}")
    //@RequiresPermissions("product:attr:list")
    public R listSearch(@RequestParam Map<String, Object> params, @PathVariable String type, @PathVariable Long catId){
        PageUtils page = attrService.listSearch(params, type, catId);

        return R.ok().put("page", page);
    }

    /**
     * 根据spuId获取产品属性值
     * @param spuId
     * @return
     */
    @RequestMapping("/base/listforspu/{spuId}")
    public R listProductAttrBySpuId(@PathVariable Long spuId){
        List<ProductAttrValueEntity> data = attrService.listProductAttrBySpuId(spuId);

        return R.ok().put("data", data);
    }

    /**
     * 根据spuId获取产品属性值
     * @param spuId
     * @return
     */
    @RequestMapping("/update/{spuId}")
    public R updateProductAttrBySpuId(@PathVariable Long spuId, @RequestBody List<ProductAttrValueEntity> productAttrValues){
        attrService.updateProductAttrBySpuId(spuId, productAttrValues);

        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrEntity attrEntity = attrService.getAttrById(attrId);
        return R.ok().put("attr", attrEntity);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrEntity attr){
		attrService.save(attr);

        return R.ok();
    }

    /**
     * 保存属性和属性分组
     */
    @RequestMapping("/group/save")
    //@RequiresPermissions("product:attr:save")
    public R saveAndGroup(@RequestBody AttrEntity attr){
        attrService.saveAndGroup(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/group/update")
    //@RequiresPermissions("product:attr:update")
    public R updateAndGroupById(@RequestBody AttrEntity attr){
		attrService.updateAndGroupById(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
        attrService.deleteAndGroup(attrIds);
        return R.ok();
    }

}
