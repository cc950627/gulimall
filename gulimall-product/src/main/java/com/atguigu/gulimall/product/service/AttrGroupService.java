package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 16:39:44
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    boolean deleteByIds(List<Long> attrGroupIds);

    List<AttrEntity> listAttrGroupByAttrGroupId(Long attrGroupId);

    PageUtils listAttrByAttrGroupId(Long attrGroupId, Map<String, Object> params);

    boolean saveAttrRelation(List<AttrAttrgroupRelationEntity> attrAttrgroupRelations);

    List<AttrGroupEntity> withattrByCatelogId(Long catelogId);
}

