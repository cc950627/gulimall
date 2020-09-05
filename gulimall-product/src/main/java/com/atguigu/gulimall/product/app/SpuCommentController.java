package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.gulimall.product.entity.SpuCommentEntity;
import com.atguigu.gulimall.product.service.SpuCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 商品评价
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-06-14 19:09:41
 */
@RestController
@RequestMapping("product/spucomment")
public class SpuCommentController {
    @Autowired
    private SpuCommentService SpuCommentService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:spucomment:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = SpuCommentService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:spucomment:info")
    public R info(@PathVariable("id") Long id){
		SpuCommentEntity SpuComment = SpuCommentService.getById(id);

        return R.ok().put("SpuComment", SpuComment);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:spucomment:save")
    public R save(@RequestBody SpuCommentEntity SpuComment){
		SpuCommentService.save(SpuComment);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:spucomment:update")
    public R update(@RequestBody SpuCommentEntity SpuComment){
		SpuCommentService.updateById(SpuComment);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:spucomment:delete")
    public R delete(@RequestBody Long[] ids){
		SpuCommentService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
