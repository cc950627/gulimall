package com.atguigu.gulimall.search.service;

import com.atguigu.common.to.es.SkuEsModel;

import java.util.List;

public interface ProductSearchService {

    boolean productSave(List<SkuEsModel> skuEsModels);

    boolean updateHasStock(List<SkuEsModel> skuEsModels);
}
