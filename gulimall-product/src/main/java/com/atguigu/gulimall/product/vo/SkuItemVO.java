package com.atguigu.gulimall.product.vo;

import com.atguigu.gulimall.product.entity.SkuImagesEntity;
import com.atguigu.gulimall.product.entity.SkuInfoEntity;
import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVO {

    private SkuInfoEntity skuInfo;

    private Boolean hasStock;

    private List<SkuImagesEntity> images;

    private SpuInfoDescEntity spuInfoDesc;

    private List<SkuAttrVO> saleAttrs;

    private List<SpuAttrGroupVO> attrGroups;

    private SeckillInfoVO seckillInfo;
}
