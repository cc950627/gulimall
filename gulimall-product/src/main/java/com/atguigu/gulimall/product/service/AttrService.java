package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 16:39:44
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean saveAndGroup(AttrEntity attr);

    PageUtils listSearch(Map<String, Object> params, String type, Long catId);

    AttrEntity getAttrById(Long attrId);

    boolean updateAndGroupById(AttrEntity attr);

    boolean deleteAndGroup(Long[] attrIds);

    List<ProductAttrValueEntity> listProductAttrBySpuId(Long spuId);

    boolean updateProductAttrBySpuId(Long spuId, List<ProductAttrValueEntity> productAttrValues);
}

