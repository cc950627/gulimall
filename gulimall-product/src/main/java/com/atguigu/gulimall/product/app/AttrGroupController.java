package com.atguigu.gulimall.product.app;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 属性分组
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 17:36:56
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrGroupService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 获取商品分类下的属性组
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R listByCatelogId(@RequestParam Map<String, Object> params, @PathVariable(value = "catelogId") Long catelogId){
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }

    /**
     * 获取分类下的所有属性分组和属性
     */
    @RequestMapping("/{catelogId}/withattr")
    //@RequiresPermissions("product:attrgroup:list")
    public R withattrByCatelogId(@PathVariable(value = "catelogId") Long catelogId){
        List<AttrGroupEntity> attrGroups = attrGroupService.withattrByCatelogId(catelogId);
        return R.ok().put("data", attrGroups);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        List<Long> catelogIds = categoryService.findCategoryIds(attrGroup.getCatelogId());
        attrGroup.setCatelogIds(catelogIds);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 属性分组关联的属性
     */
    @RequestMapping("/{attrGroupId}/attr/relation")
    //@RequiresPermissions("product:attr:info")
    public R listAttrGroupByAttrGroupId(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> attrEntity = attrGroupService.listAttrGroupByAttrGroupId(attrGroupId);
        return R.ok().put("attr", attrEntity);
    }

    /**
     * 新建属性分组关联的属性
     */
    @RequestMapping("/{attrGroupId}/noattr/relation")
    //@RequiresPermissions("product:attr:info")
    public R listAttrByAttrGroupId(@PathVariable("attrGroupId") Long attrGroupId, @RequestParam Map<String, Object> params){
        PageUtils attrEntity = attrGroupService.listAttrByAttrGroupId(attrGroupId, params);
        return R.ok().put("page", attrEntity);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 保存分组下的属性
     */
    @RequestMapping("/attr/relation")
    //@RequiresPermissions("product:attrgroup:save")
    public R saveAttrRelation(@RequestBody List<AttrAttrgroupRelationEntity> attrAttrgroupRelations){
        attrGroupService.saveAttrRelation(attrAttrgroupRelations);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.deleteByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
