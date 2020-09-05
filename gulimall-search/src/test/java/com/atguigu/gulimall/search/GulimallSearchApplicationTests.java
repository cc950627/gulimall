package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.google.common.collect.Maps;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void contextLoads() {
        System.out.println(restHighLevelClient);
    }

    @Test
    public void indexDate() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("2");
        String jsonString = "{\"username\":\"李四\",\"age\":20}";
        indexRequest.source(jsonString, XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(indexResponse);
    }

    @Test
    public void searchData() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(EsConstant.PRODUCT_INDEX);
        /*SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchQuery("username", "张三"));
        searchRequest.source(builder);*/
        SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(response);
    }

    @Test
    public void searchData2() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("users");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchAllQuery());

        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("age_terms").field("age");
        builder.aggregation(termsAggregationBuilder).size(10);

        AvgAggregationBuilder avgAggregationBuilder = AggregationBuilders.avg("age_avg").field("age");
        builder.aggregation(avgAggregationBuilder).size(10);

        searchRequest.source(builder);
        SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

        SearchHits hits = response.getHits();
        Arrays.stream(hits.getHits()).forEach(e -> System.out.println(e.getSourceAsMap()));

        Aggregations aggregations = response.getAggregations();
        Terms ageTerms = aggregations.get("age_terms");
        Map<String, Long> avgMap = ageTerms.getBuckets().stream().collect(Collectors.toMap(Terms.Bucket::getKeyAsString, Terms.Bucket::getDocCount));
        System.out.println(avgMap);

        Avg ageAvg = aggregations.get("age_avg");
        System.out.println(ageAvg.getValueAsString());
    }

    @Test
    public void deleteIndex() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest(EsConstant.PRODUCT_INDEX);
        restHighLevelClient.delete(deleteRequest, ElasticSearchConfig.COMMON_OPTIONS);
    }

    @Test
    public void updateData() throws IOException {
        UpdateRequest updateRequest = new UpdateRequest(EsConstant.PRODUCT_INDEX, "7");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("brandImg", "https://study-chengcheng.oss-cn-beijing.aliyuncs.com/2020-07-13/2032812d-6681-4500-80f3-77e83a93c38b_apple.png");
        updateRequest.doc(jsonObject.toJSONString(), XContentType.JSON);
        UpdateResponse response = restHighLevelClient.update(updateRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(response);
    }

}
