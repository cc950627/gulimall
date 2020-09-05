package com.atguigu.gulimall.search.controller;

import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProductSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search/es")
public class ElasticSearchController {

    @Autowired
    private ProductSearchService productSearchService;

    /**
     * 索引商品
     * @param skuEsModels
     * @return
     */
    @PostMapping("/product/save")
    public R productSave(@RequestBody List<SkuEsModel> skuEsModels) {
        productSearchService.productSave(skuEsModels);
        return R.ok();
    }

    /**
     * 更新商品库存索引
     * @param skuEsModels
     * @return
     */
    @PostMapping("/product/updateHasStock")
    R updateHasStock(@RequestBody List<SkuEsModel> skuEsModels) {
        productSearchService.updateHasStock(skuEsModels);
        return R.ok();
    }
}
