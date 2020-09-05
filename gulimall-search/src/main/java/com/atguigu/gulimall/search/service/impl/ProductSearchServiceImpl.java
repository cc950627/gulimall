package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service("ProductSearchService")
public class ProductSearchServiceImpl implements ProductSearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productSave(List<SkuEsModel> skuEsModels) {
        AtomicInteger atomicInteger = new AtomicInteger();
        while (true) {
            try {
                atomicInteger.incrementAndGet();
                BulkRequest bulkRequest = new BulkRequest();
                skuEsModels.stream().forEach(e -> {
                    IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
                    indexRequest.id(String.valueOf(e.getSkuId()));
                    indexRequest.source(JSON.toJSONString(e), XContentType.JSON);
                    bulkRequest.add(indexRequest);
                });
                BulkResponse response = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);
                if (!response.hasFailures()) {
                    return true;
                }
                if (atomicInteger.get() >= EsConstant.retry) {
                    throw new Exception(String.format("elasticsearch保存商品信息失败"));
                }
                BulkItemResponse[] items = response.getItems();
                List<Long> skuIds = Stream.of(items).filter(BulkItemResponse::isFailed).map(e -> Long.parseLong(e.getId())).collect(Collectors.toList());
                skuEsModels = skuEsModels.stream().filter(e -> skuIds.contains(e.getSkuId())).collect(Collectors.toList());
            } catch (Exception e) {
                log.error(e.getMessage(), skuEsModels.stream().map(o -> String.valueOf(o.getSkuId())).collect(Collectors.joining(",")));
                if (atomicInteger.get() >= EsConstant.retry) {
                    throw new BizException(BizExceptionEnum.S_ES_OPERATION_ERROR, String.format("elasticsearch保存商品信息失败"));
                }
            }
        }
    }

    @Override
    public boolean updateHasStock(List<SkuEsModel> skuEsModels) {
        skuEsModels.stream().forEach(e -> {
            UpdateRequest updateRequest = new UpdateRequest(EsConstant.PRODUCT_INDEX, String.valueOf(e.getSkuId()));
            updateRequest.doc(JSON.toJSONString(e), XContentType.JSON);
            this.update(updateRequest, EsConstant.retry);
        });
        return true;
    }

    private UpdateResponse update(UpdateRequest updateRequest, int retry) {
        if (retry <= 0) {
            throw new BizException(BizExceptionEnum.S_ES_OPERATION_ERROR, String.format("elasticsearch更新商品库存失败"));
        }
        try {
            return restHighLevelClient.update(updateRequest, ElasticSearchConfig.COMMON_OPTIONS);
        } catch (Exception e) {
            return update(updateRequest, --retry);
        }
    }
}
