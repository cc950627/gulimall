package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.Constant;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.feign.ProductFeignService;
import com.atguigu.gulimall.search.service.SearchService;
import com.atguigu.gulimall.search.vo.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchServiceImpl implements SearchService {

    private final String PRICE_FLAG = "_";

    private final String ATTR_FLAG = ":";


    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult pageListByCondition(SearchParam param) {
        SearchResult searchResult = null;
        try {
            SearchRequest searchRequest = bulidSearchRequest(param);
            System.out.println(searchRequest.source().toString());
            SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            searchResult = bulidSearchResponse(param, response);
        } catch (Exception e) {
            throw new BizException(BizExceptionEnum.S_ES_OPERATION_ERROR, "搜索商品信息操作失败");
        }
        return searchResult;
    }

    private SearchRequest bulidSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(param.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        Optional.ofNullable(param.getCatalogId()).ifPresent(e -> boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", e)));
        if (CollectionUtils.isNotEmpty(param.getBrandIds())) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandIds()));
        }
        Boolean hasStock = Optional.ofNullable(param.getHasStock()).orElse(true);
        boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", hasStock));
        if (StringUtils.contains(param.getSkuPrice(), PRICE_FLAG)) {
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            String[] priceArr = StringUtils.split(param.getSkuPrice(), PRICE_FLAG);
            if (StringUtils.startsWith(param.getSkuPrice(), PRICE_FLAG)) {
                skuPrice.lte(priceArr[0]);
            } else if (StringUtils.endsWith(param.getSkuPrice(), PRICE_FLAG)) {
                skuPrice.gte(priceArr[0]);
            } else {
                skuPrice.gte(priceArr[0]).lte(priceArr[1]);
            }
            boolQueryBuilder.filter(skuPrice);
        }
        if (CollectionUtils.isNotEmpty(param.getAttrs())) {
            param.getAttrs().forEach(e -> {
                String[] attr = StringUtils.split(e, PRICE_FLAG);
                String[] attrValues = StringUtils.split(attr[1], ATTR_FLAG);
                BoolQueryBuilder nestBoolQueryBuilder = QueryBuilders.boolQuery();
                nestBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attr[0]));
                nestBoolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", nestBoolQueryBuilder, ScoreMode.None);
                boolQueryBuilder.filter(attrs);
            });
        }
        sourceBuilder.query(boolQueryBuilder);

        // 排序
        if (StringUtils.isNoneBlank(param.getSort())) {
            String sort = param.getSort();
            String[] sortArr = sort.split(PRICE_FLAG);
            sourceBuilder.sort(sortArr[0], SortOrder.fromString(sortArr[1]));
        }

        // 分页
        param.initPage(Constant.PageDefaut.SEARCH_LIST);
        sourceBuilder.from((param.getCurrPage() - 1) * param.getPageSize());
        sourceBuilder.size(param.getPageSize());

        // 高亮
        if (StringUtils.isNoneBlank(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        // 聚合分析
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brandAgg);
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalogAgg);
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(20));
        attrAgg.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(attrAgg);

        SearchRequest request = new SearchRequest();
        request.indices(EsConstant.PRODUCT_INDEX);
        request.source(sourceBuilder);
        return request;
    }

    private SearchResult bulidSearchResponse(SearchParam param, SearchResponse response) {
        SearchResult searchResult = new SearchResult();
        // 商品信息
        SearchHits hits = response.getHits();
        List<SkuEsModel> products = Stream.of(hits.getHits()).map(e -> {
            SkuEsModel skuEsModel = JSON.parseObject(e.getSourceAsString(), SkuEsModel.class);
            HighlightField skuTitle = e.getHighlightFields().get("skuTitle");
            Optional.ofNullable(skuTitle).ifPresent(o -> Stream.of(o.getFragments()).findAny().ifPresent(x -> skuEsModel.setSkuTitle(x.string())));
            return skuEsModel;
        }).collect(Collectors.toList());
        searchResult.setProducts(products);

        // 分页信息
        long total = hits.getTotalHits().value;
        searchResult.setCurrPage(param.getCurrPage());
        searchResult.setPageSize(param.getPageSize());
        searchResult.setTotalCount(total);
        searchResult.setTotalPage((long) Math.ceil((double) total / param.getPageSize()));

        // 品牌信息
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        List<BrandVO> brands = brandAgg.getBuckets().stream().map(e -> {
            BrandVO brandVO = new BrandVO();
            brandVO.setBrandId(e.getKeyAsNumber().longValue());
            ParsedStringTerms brandNameAgg = e.getAggregations().get("brand_name_agg");
            brandNameAgg.getBuckets().stream().findAny().ifPresent(o -> brandVO.setBrandName(o.getKeyAsString()));
            ParsedStringTerms brandImgAgg = e.getAggregations().get("brand_img_agg");
            brandImgAgg.getBuckets().stream().findAny().ifPresent(o -> brandVO.setBrandImg(o.getKeyAsString()));
            return brandVO;
        }).collect(Collectors.toList());
        searchResult.setBrands(brands);
        // 分类信息
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        List<CatalogVO> catalogs = catalogAgg.getBuckets().stream().map(e -> {
            CatalogVO catalogVO = new CatalogVO();
            catalogVO.setCatalogId(e.getKeyAsNumber().longValue());
            ParsedStringTerms catalogNameAgg = e.getAggregations().get("catalog_name_agg");
            catalogNameAgg.getBuckets().stream().findAny().ifPresent(o -> catalogVO.setCatalogName(o.getKeyAsString()));
            return catalogVO;
        }).collect(Collectors.toList());
        searchResult.setCatalogs(catalogs);
        //属性信息
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        List<AttrVO> attrs = attrIdAgg.getBuckets().stream().map(e -> {
            AttrVO attrVO = new AttrVO();
            attrVO.setAttrId(e.getKeyAsNumber().longValue());
            ParsedStringTerms attrNameAgg = e.getAggregations().get("attr_name_agg");
            attrNameAgg.getBuckets().stream().findAny().ifPresent(o -> attrVO.setAttrName(o.getKeyAsString()));
            ParsedStringTerms attrValueAgg = e.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVO.setAttrValue(attrValues);
            return attrVO;
        }).collect(Collectors.toList());
        searchResult.setAttrs(attrs);
        //页面属性导航
        Optional.ofNullable(param.getAttrs()).ifPresent(e -> {
            List<NavVO> navVOs = e.stream().map(o -> {
                NavVO navVO = new NavVO();
                String[] attr = StringUtils.split(o, PRICE_FLAG);
                navVO.setNavValue(attr[1]);
                R r = productFeignService.info(Long.parseLong(attr[0]));
                if (!Objects.equals(0, r.getCode())) {
                    throw new BizException(BizExceptionEnum.P_REQ_REMOTESERVICE_FAIL,
                            String.format("server-name：gulimall-product，url：/product/attr/info/{attrId}，param：%s", attr[0]), r);
                }
                JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(r.get("attr")));
                String attrName = jsonObject.getString("attrName");
                navVO.setNavName(attrName);
                String replace = null;
                try {
                    String attrValue = URLEncoder.encode(o, "UTF-8").replace("+", "%20");
                    replace = param.get_queryString().replace(String.format("&attrs=%s", attrValue), "")
                            .replace(String.format("attrs=%s", attrValue), "");
                } catch (UnsupportedEncodingException unsupportedEncodingException) {
                    unsupportedEncodingException.printStackTrace();
                }
                navVO.setLink(String.format("http://search.gulimall.com/list.html?%s", replace));
                return navVO;
            }).collect(Collectors.toList());
            searchResult.setNavs(navVOs);
        });
        return searchResult;
    }
}
