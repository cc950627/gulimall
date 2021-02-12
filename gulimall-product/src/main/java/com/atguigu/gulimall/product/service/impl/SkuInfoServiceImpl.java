package com.atguigu.gulimall.product.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.product.enums.AttrTypeEnum;
import com.atguigu.gulimall.product.dao.SkuInfoDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.SeckillFeginService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.query.ProductQuery;
import com.atguigu.gulimall.product.service.*;
import com.atguigu.gulimall.product.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SeckillFeginService seckillFeginService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCoundtion(Map<String, Object> params) {
        IPage<SkuInfoEntity> iPage = new Query<SkuInfoEntity>().getPage(params);
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        Object key = params.get("key");
        if (Objects.nonNull(key) && StringUtils.isNotBlank(String.valueOf(key))) {
            wrapper.and(e -> e.eq("sku_id", key).or().like("sku_name", key));
        }
        Object catelogId = params.get("catelogId");
        if (Objects.nonNull(catelogId) && StringUtils.isNotBlank(String.valueOf(catelogId)) && !Objects.equals(catelogId, "0")) {
            wrapper.eq("catalog_id", Long.parseLong(String.valueOf(catelogId)));
        }
        Object brandId = params.get("brandId");
        if (Objects.nonNull(brandId) && StringUtils.isNotBlank(String.valueOf(brandId)) && !Objects.equals(brandId, "0")) {
            wrapper.eq("brand_id", Long.parseLong(String.valueOf(brandId)));
        }
        Object min = params.get("min");
        if (Objects.nonNull(min) && StringUtils.isNotBlank(String.valueOf(min))) {
            wrapper.ge("price", new BigDecimal(String.valueOf(min).trim()));
        }
        Object max = params.get("max");
        if (Objects.nonNull(max) && StringUtils.isNotBlank(String.valueOf(max))) {
            BigDecimal bigDecimal = new BigDecimal(String.valueOf(max).trim());
            if (bigDecimal.compareTo(BigDecimal.ZERO) == 1) {
                wrapper.le("price", bigDecimal);
            }
        }
        IPage<SkuInfoEntity> page = this.page(iPage, wrapper);
        return new PageUtils(page);

    }

    @Override
    public List<SkuInfoEntity> listByCoundtion(ProductQuery params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        String key = params.getKey();
        if (StringUtils.isNotBlank(key)) {
            wrapper.like("sku_name", key);
        }
        Long catelogId = params.getCatelogId();
        if (Objects.nonNull(catelogId) && !Objects.equals(catelogId, 0L)) {
            wrapper.eq("catalog_id", catelogId);
        }
        return this.list(wrapper);
    }

    @Override
    public List<Long> getCatelogIdsBySkuId(Long skuId) {
        SkuInfoEntity skuInfo = this.getById(skuId);
        return categoryService.findCategoryIds(skuInfo.getCatalogId());
    }

    @Override
    public SkuItemVO skuItem(Long skuId) {
        SkuItemVO skuItem = new SkuItemVO();
        CompletableFuture<SkuInfoEntity> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfo = this.getById(skuId);
            skuItem.setSkuInfo(skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

        CompletableFuture<Set<Long>> skuIdsFuture = skuInfoFuture.thenApplyAsync(e -> {
            QueryWrapper<SkuInfoEntity> skuInfoEntityQueryWrapper = new QueryWrapper<>();
            skuInfoEntityQueryWrapper.eq("spu_id", e.getSpuId());
            List<SkuInfoEntity> skuInfoEntities = skuInfoService.list(skuInfoEntityQueryWrapper);
            return skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toSet());
        }, threadPoolExecutor);

        CompletableFuture<Void> spuInfoFuture = skuInfoFuture.thenAcceptAsync(e ->{
            QueryWrapper<SpuInfoDescEntity> spuInfoDescEntityQueryWrapper = new QueryWrapper<>();
            spuInfoDescEntityQueryWrapper.eq("spu_id", e.getSpuId());
            SpuInfoDescEntity SpuInfoDescs = spuInfoDescService.getOne(spuInfoDescEntityQueryWrapper);
            skuItem.setSpuInfoDesc(SpuInfoDescs);
        }, threadPoolExecutor);

        CompletableFuture<Map<Boolean, Set<Long>>> saleAndBaseIdFuture = skuInfoFuture.thenApplyAsync(e -> {
            QueryWrapper<AttrEntity> attrEntityQueryWrapper = new QueryWrapper<>();
            attrEntityQueryWrapper.eq("catelog_id", e.getCatalogId());
            List<AttrEntity> attrs = attrService.list(attrEntityQueryWrapper);
            return attrs.stream().collect(Collectors.partitioningBy(o -> Objects.equals(o.getAttrType(),
                    AttrTypeEnum.ATTR_TYPE_SALE.getValue()), Collectors.mapping(AttrEntity::getAttrId, Collectors.toSet())));
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrsFuture = skuIdsFuture.thenAcceptBothAsync(saleAndBaseIdFuture, (e, o) ->{
            Set<Long> saleIdList = o.get(true);
            if (CollectionUtils.isNotEmpty(saleIdList)) {
                QueryWrapper<SkuSaleAttrValueEntity> skuSaleAttrValueEntityQueryWrapper = new QueryWrapper<>();
                skuSaleAttrValueEntityQueryWrapper.in("sku_id", e);
                skuSaleAttrValueEntityQueryWrapper.in("attr_id", saleIdList);
                List<SkuSaleAttrValueEntity> saleAttrValueEntityList = skuSaleAttrValueService.list(skuSaleAttrValueEntityQueryWrapper);
                Map<Long, List<SkuSaleAttrValueEntity>> saleMap = saleAttrValueEntityList.stream().collect(Collectors.groupingBy(SkuSaleAttrValueEntity::getAttrId));
                if (MapUtils.isNotEmpty(saleMap)) {
                    List<SkuAttrVO> attrVOList = saleMap.entrySet().stream().map(x -> {
                        SkuAttrVO skuAttrVO = new SkuAttrVO();
                        skuAttrVO.setAttrId(x.getKey());
                        x.getValue().stream().findAny().ifPresent(y -> skuAttrVO.setAttrName(y.getAttrName()));

                        Map<String, List<Long>> skuIdAttrValueMap = x.getValue().stream().collect(Collectors.groupingBy(
                                SkuSaleAttrValueEntity::getAttrValue, Collectors.mapping(SkuSaleAttrValueEntity::getSkuId, Collectors.toList())));
                        List<SkuIdAttrValueVO> skuIdAttrValues = skuIdAttrValueMap.entrySet().stream().map(y -> {
                            SkuIdAttrValueVO skuIdAttrValueVO = new SkuIdAttrValueVO();
                            skuIdAttrValueVO.setAttrValue(y.getKey());
                            skuIdAttrValueVO.setSkuIds(y.getValue());
                            return skuIdAttrValueVO;
                        }).collect(Collectors.toList());
                        skuAttrVO.setSkuIdAttrValues(skuIdAttrValues);
                        return skuAttrVO;
                    }).collect(Collectors.toList());
                    skuItem.setSaleAttrs(attrVOList);
                }
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> attrGroupsFuture = saleAndBaseIdFuture.thenAcceptBothAsync(skuInfoFuture, (e, o) -> {
            Set<Long> baseIdList = e.get(false);
            if (CollectionUtils.isNotEmpty(baseIdList)) {
                QueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationQueryWrapper = new QueryWrapper<>();
                attrAttrgroupRelationQueryWrapper.in("attr_id", baseIdList);
                List<AttrAttrgroupRelationEntity> attrgroupRelations = attrAttrgroupRelationService.list(attrAttrgroupRelationQueryWrapper);
                Map<Long, Set<Long>> attrGroupMap = attrgroupRelations.stream().collect(Collectors.groupingBy(AttrAttrgroupRelationEntity::getAttrGroupId,
                        Collectors.mapping(AttrAttrgroupRelationEntity::getAttrId, Collectors.toSet())));

                QueryWrapper<ProductAttrValueEntity> productAttrValueQueryWrapper = new QueryWrapper<>();
                productAttrValueQueryWrapper.eq("spu_id", o.getSpuId());
                productAttrValueQueryWrapper.in("attr_id", baseIdList);
                List<ProductAttrValueEntity> productAttrValues = productAttrValueService.list(productAttrValueQueryWrapper);
                Map<Long, Map<String, List<String>>> productAttrGroupMap = productAttrValues.stream().collect(Collectors.groupingBy(x ->
                                attrGroupMap.entrySet().stream().filter(y -> y.getValue().contains(x.getAttrId())).findAny().get().getKey(),
                        Collectors.groupingBy(ProductAttrValueEntity::getAttrName, Collectors.mapping(ProductAttrValueEntity::getAttrValue, Collectors.toList()))));

                if (MapUtils.isNotEmpty(attrGroupMap)) {
                    List<AttrGroupEntity> attrGroupEntities = attrGroupService.listByIds(attrGroupMap.keySet());
                    List<SpuAttrGroupVO> attrGroups = attrGroupEntities.stream().map(x -> {
                        SpuAttrGroupVO spuAttrGroupVO = new SpuAttrGroupVO();
                        spuAttrGroupVO.setGroupName(x.getAttrGroupName());
                        List<SkuAttrVO> skuAttrVOS = productAttrGroupMap.get(x.getAttrGroupId()).entrySet().stream().map(y -> {
                            SkuAttrVO skuAttrVO = new SkuAttrVO();
                            skuAttrVO.setAttrName(y.getKey());
                            List<SkuIdAttrValueVO> skuIdAttrValueVOS = y.getValue().stream().map(z -> {
                                SkuIdAttrValueVO skuIdAttrValueVO = new SkuIdAttrValueVO();
                                skuIdAttrValueVO.setAttrValue(z);
                                return skuIdAttrValueVO;
                            }).collect(Collectors.toList());
                            skuAttrVO.setSkuIdAttrValues(skuIdAttrValueVOS);
                            return skuAttrVO;
                        }).collect(Collectors.toList());
                        spuAttrGroupVO.setSpuAttrs(skuAttrVOS);
                        return spuAttrGroupVO;
                    }).collect(Collectors.toList());
                    skuItem.setAttrGroups(attrGroups);
                }
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            QueryWrapper<SkuImagesEntity> skuImagesEntityQueryWrapper = new QueryWrapper<>();
            skuImagesEntityQueryWrapper.eq("sku_id", skuId);
            List<SkuImagesEntity> skuImagesEntities = skuImagesService.list(skuImagesEntityQueryWrapper);
            skuItem.setImages(skuImagesEntities);
        }, threadPoolExecutor);

        CompletableFuture<Void> hasStockFuture = CompletableFuture.runAsync(() -> {
            R r = wareFeignService.getSkuStock(Lists.newArrayList(skuId));
            if (!Objects.equals(0, r.getCode())) {
                throw new BizException(BizExceptionEnum.P_REQ_REMOTESERVICE_FAIL,
                        String.format("server-name：gulimall-ware，url：/ware/waresku/getSkuStock，param：%s", skuId), r);
            }
            JSONObject data = JSON.parseObject(JSON.toJSONString(r.get("data")));
            skuItem.setHasStock(data.getInteger(String.valueOf(skuId)) > 0);
        }, threadPoolExecutor);

        CompletableFuture<Void> seckillInfoFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeginService.getSkuSeckillInfo(skuId);
            if (Objects.nonNull(r.get("data"))) {
                SeckillInfoVO seckillInfoVO = JSON.parseObject(JSON.toJSONString(r.get("data")), SeckillInfoVO.class);
                skuItem.setSeckillInfo(seckillInfoVO);
            }
        }, threadPoolExecutor);

        CompletableFuture.allOf(spuInfoFuture, saleAttrsFuture, attrGroupsFuture, imagesFuture, hasStockFuture, seckillInfoFuture).join();
        return skuItem;
    }

    @SentinelResource(value = "getPriceResource", blockHandler = "blockHandler")
    @Override
    public BigDecimal getPrice(Long skuId) {
        SkuInfoEntity skuInfoEntity = this.getById(skuId);
        Optional.ofNullable(skuInfoEntity).orElseThrow(() -> new BizException(BizExceptionEnum.P_REQUEST_PARAM_ERROR, String.format("商品Id：%s不存在", skuId)));
        return skuInfoEntity.getPrice();
    }

    public BigDecimal blockHandler(Long skuId, BlockException exception) {
        System.out.println(skuId + exception.getMessage());
        return BigDecimal.ZERO;
    }

}
