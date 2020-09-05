package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.to.SpuBoundTO;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.gulimall.product.constant.SpuStatusEnum;
import com.atguigu.gulimall.product.dao.SpuInfoDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.feign.CouponFeignService;
import com.atguigu.gulimall.product.feign.SearchFeignService;
import com.atguigu.gulimall.product.feign.WareFeignService;
import com.atguigu.gulimall.product.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


@Service("SpuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean saveSpuInfo(SpuInfoEntity spuInfo) {
         spuInfo.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        spuInfo.setUpdateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        this.save(spuInfo);

        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfo.getId());
        spuInfoDescEntity.setDecript(String.join(",", spuInfo.getDecript()));
        spuInfoDescService.save(spuInfoDescEntity);

        List<String> images = Optional.ofNullable(spuInfo.getImages()).orElseGet(Lists::newArrayList);
        List<SpuImagesEntity> spuImagesList = images.stream().map(e -> {
            SpuImagesEntity spuImages = new SpuImagesEntity();
            spuImages.setSpuId(spuInfo.getId());
            spuImages.setImgUrl(e);
            return spuImages;
        }).collect(Collectors.toList());
        spuImagesService.saveBatch(spuImagesList);

        List<ProductAttrValueEntity> baseAttrs = Optional.ofNullable(spuInfo.getBaseAttrs()).orElseGet(Lists::newArrayList);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<AttrEntity> attrs = attrService.listByIds(attrIds);
        baseAttrs.stream().map(e -> {
            attrs.stream().filter(o -> Objects.equals(o.getAttrId(), e.getAttrId())).findAny().ifPresent(o -> e.setAttrName(o.getAttrName()));
            e.setAttrValue(e.getAttrValues());
            e.setQuickShow(e.getShowDesc());
            e.setSpuId(spuInfo.getId());
            return e.getAttrId();
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(baseAttrs);

        List<SkuInfoEntity> skus = Optional.ofNullable(spuInfo.getSkus()).orElseGet(Lists::newArrayList);
        skus.forEach(e -> {
            e.setSpuId(spuInfo.getId());
            e.setBrandId(spuInfo.getBrandId());
            e.setCatalogId(spuInfo.getCatalogId());
            e.setSaleCount(0L);
            List<SkuImagesEntity> skuImages = Optional.ofNullable(e.getImages()).orElseGet(Lists::newArrayList);
            skuImages.stream().filter(o -> Objects.equals(o.getDefaultImg(), 1)).findAny().ifPresent(o -> e.setSkuDefaultImg(o.getImgUrl()));
        });
        skuInfoService.saveBatch(skus);

        skus.parallelStream().forEach(e -> Optional.ofNullable(e.getImages()).ifPresent(o -> o.forEach(x -> x.setSkuId(e.getSkuId()))));
        List<SkuImagesEntity> skuImages = skus.stream().map(SkuInfoEntity::getImages).flatMap(Collection::stream)
                .filter(e -> StringUtils.isNotBlank(e.getImgUrl())).collect(Collectors.toList());
        skuImagesService.saveBatch(skuImages);

        skus.parallelStream().forEach(e -> Optional.ofNullable(e.getAttr()).ifPresent(o -> o.forEach(x -> x.setSkuId(e.getSkuId()))));
        List<SkuSaleAttrValueEntity> skuSaleAttrValues = skus.stream().map(SkuInfoEntity::getAttr).flatMap(Collection::stream).collect(Collectors.toList());
        skuSaleAttrValueService.saveBatch(skuSaleAttrValues);

        SpuBoundTO bounds = spuInfo.getBounds();
        bounds.setSpuId(spuInfo.getId());
        R r1 = couponFeignService.saveSpuBounds(bounds);
        if (!Objects.equals(0, r1.getCode())) {
            throw new BizException(BizExceptionEnum.P_REQ_REMOTESERVICE_FAIL,
                    String.format("server-name：gulimall-coupon，url：/coupon/spubounds/save，param：%s", JSON.toJSONString(bounds)), r1);
        }

        List<SkuInfoEntity> skuList = skus.stream().filter(e -> (e.getFullCount() > 0 || Objects.equals(e.getFullPrice()
                .compareTo(BigDecimal.valueOf(0)),1))).collect(Collectors.toList());
        R r2 = couponFeignService.saveSkuReduction(skuList);
        if (!Objects.equals(0, r1.getCode())) {
            throw new BizException(BizExceptionEnum.P_REQ_REMOTESERVICE_FAIL,
                    String.format("server-name：gulimall-coupon，url：/coupon/skufullreduction/saveInfo，param：%s", JSON.toJSONString(skus)), r2);
        }
        return true;
    }

    @Override
    public PageUtils queryPageByCoundtion(Map<String, Object> params) {
        IPage<SpuInfoEntity> iPage = new Query<SpuInfoEntity>().getPage(params);
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        Object key = params.get("key");
        if (Objects.nonNull(key) && StringUtils.isNotBlank(String.valueOf(key))) {
            wrapper.and(e -> e.eq("id", key).or().like("spu_name", key));
        }
        Object status = params.get("status");
        if (Objects.nonNull(status) && StringUtils.isNotBlank(String.valueOf(status))) {
            wrapper.eq("publish_status", Integer.parseInt(String.valueOf(status)));
        }
        Object brandId = params.get("brandId");
        if (Objects.nonNull(brandId) && StringUtils.isNotBlank(String.valueOf(brandId)) && !Objects.equals(brandId, "0")) {
            wrapper.eq("brand_id", Long.parseLong(String.valueOf(brandId)));
        }
        Object catelogId = params.get("catelogId");
        if (Objects.nonNull(catelogId) && StringUtils.isNotBlank(String.valueOf(catelogId)) && !Objects.equals(catelogId, "0")) {
            wrapper.eq("catalog_id", Long.parseLong(String.valueOf(catelogId)));
        }
        IPage<SpuInfoEntity> page = this.page(iPage, wrapper);

        List<SpuInfoEntity> spuInfos = page.getRecords();
        Set<Long> catalogIds = spuInfos.stream().map(SpuInfoEntity::getCatalogId).collect(Collectors.toSet());
        Set<Long> brandIds = spuInfos.stream().map(SpuInfoEntity::getBrandId).collect(Collectors.toSet());
        List<CategoryEntity> categorys = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(catalogIds)) {
            categorys.addAll(categoryService.listByIds(catalogIds));
        }
        List<BrandEntity> brands = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(brandIds)) {
            brands.addAll(brandService.listByIds(brandIds));
        }
        spuInfos.forEach(e -> {
            categorys.stream().filter(o -> Objects.equals(o.getCatId(), e.getCatalogId())).findAny().ifPresent(o -> e.setCatalogName(o.getName()));
            brands.stream().filter(o -> Objects.equals(o.getBrandId(), e.getBrandId())).findAny().ifPresent(o -> e.setBrandName(o.getName()));
        });
        return new PageUtils(page);
    }

    @Override
    @Transactional
    public boolean prdouctUp(Long spuId) {

        SpuInfoEntity spuInfo = this.getById(spuId);
        Optional.ofNullable(spuInfo).orElseThrow(() -> new BizException(BizExceptionEnum.P_REQUEST_PARAM_ERROR, String.format("spuId：%sb不存在", spuId)));

        QueryWrapper<ProductAttrValueEntity> productAttrValueWrapper = new QueryWrapper<>();
        productAttrValueWrapper.eq("spu_id", spuId);
        List<ProductAttrValueEntity> productAttrValues = productAttrValueService.list(productAttrValueWrapper);

        Set<Long> attrIds = productAttrValues.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toSet());
        QueryWrapper<AttrEntity> attrWrapper = new QueryWrapper<>();
        attrWrapper.in("attr_id", attrIds);
        attrWrapper.eq("search_type", 1);
        List<AttrEntity> attrs = attrService.list(attrWrapper);

        List<SkuEsModel.Attrs> esAttrs = productAttrValues.stream().filter(e -> attrs.stream().anyMatch(o ->
                Objects.equals(o.getAttrId(), e.getAttrId()))).map(e -> {
            SkuEsModel.Attrs esAttr = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(e, esAttr);
            return esAttr;
        }).collect(Collectors.toList());

        QueryWrapper<SkuInfoEntity> SkuInfoWrapper = new QueryWrapper<>();
        SkuInfoWrapper.eq("spu_id", spuId);
        List<SkuInfoEntity> skuInfos = skuInfoService.list(SkuInfoWrapper);

        Set<Long> skuIdIds = skuInfos.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toSet());
        R r1 = wareFeignService.getSkuStock(skuIdIds);
        if (!Objects.equals(0, r1.getCode())) {
            throw new BizException(BizExceptionEnum.P_REQ_REMOTESERVICE_FAIL,
                    String.format("server-name：gulimall-ware，url：/ware/waresku/getSkuStock，param：%s", JSON.toJSONString(skuIdIds)), r1);
        }
        JSONObject data = JSON.parseObject(JSON.toJSONString(r1.get("data")));

        Set<Long> brandIds = skuInfos.stream().map(SkuInfoEntity::getBrandId).collect(Collectors.toSet());
        Set<Long> catalogIds = skuInfos.stream().map(SkuInfoEntity::getCatalogId).collect(Collectors.toSet());
        List<BrandEntity> brands = brandService.listByIds(brandIds);
        List<CategoryEntity> categorys = categoryService.listByIds(catalogIds);

        List<SkuEsModel> skuEsModels = skuInfos.stream().map(e -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(e, skuEsModel);
            skuEsModel.setSkuPrice(e.getPrice());
            skuEsModel.setSkuImg(e.getSkuDefaultImg());
            skuEsModel.setHotScore(0L);
            skuEsModel.setHasStock(data.getIntValue(String.valueOf(e.getSkuId())) > 0);
            brands.stream().filter(o -> Objects.equals(o.getBrandId(), e.getBrandId())).findAny().ifPresent(o -> {
                skuEsModel.setBrandName(o.getName());
                skuEsModel.setBrandImg(o.getLogo());
            });
            categorys.stream().filter(o -> Objects.equals(o.getCatId(), e.getCatalogId())).findAny().ifPresent(o -> skuEsModel.setCatalogName(o.getName()));
            skuEsModel.setAttrs(esAttrs);
            return skuEsModel;
        }).collect(Collectors.toList());

        R r2 = searchFeignService.productSave(skuEsModels);
        if (!Objects.equals(0, r2.getCode())) {
            throw new BizException(BizExceptionEnum.P_REQ_REMOTESERVICE_FAIL,
                    String.format("server-name：gulimall-search，url：/search/es/product/Up，param：%s", JSON.toJSONString(skuEsModels)), r2);
        }

        spuInfo.setPublishStatus(SpuStatusEnum.SPU_UP.getStatus());
        spuInfo.setUpdateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        return this.updateById(spuInfo);
    }

    @Override
    public SpuInfoEntity getSpuInfo(Long skuId) {
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
        SpuInfoEntity spuInfo = this.getById(skuInfo.getSpuId());
        BrandEntity brand = brandService.getById(spuInfo.getBrandId());
        spuInfo.setBrandName(brand.getName());
        return spuInfo;
    }

}
