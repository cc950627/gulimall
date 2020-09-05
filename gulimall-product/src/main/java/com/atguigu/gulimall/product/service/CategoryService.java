package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.vo.CatelogShowVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 16:39:44
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTreeRedisLock();

    boolean removeMeunByIds(List<Long> catIds);

    List<Long> findCategoryIds(Long catId);

    boolean updateDetailById(CategoryEntity category);

    boolean deteleMeunByIds(List<Long> catIds);

    List<CategoryEntity> listByParentCid(Long parentCid);

    Map<Long, List<CategoryEntity>> showCatalogs();
}

