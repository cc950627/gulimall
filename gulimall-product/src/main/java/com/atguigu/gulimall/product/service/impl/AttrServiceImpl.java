package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.gulimall.product.constant.AttrTypeEnum;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.*;
import com.atguigu.gulimall.product.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public boolean saveAndGroup(AttrEntity attr) {
        boolean result = this.save(attr);
        Optional.ofNullable(attr.getAttrGroupId()).ifPresent(e -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        });
        return result;
    }

    @Override
    public PageUtils listSearch(Map<String, Object> params, String type, Long catId) {
        Optional<AttrTypeEnum> optional = Optional.ofNullable(AttrTypeEnum.getAttrTypeEnum(type));
        optional.orElseThrow(() -> new BizException(BizExceptionEnum.P_GET_ATTRLIST_FAIL, String.format("无效的属性类型：%s", type)));
        IPage<AttrEntity> page = new Query<AttrEntity>().getPage(params);
        QueryWrapper<AttrEntity> attrWrapper = new QueryWrapper<>();
        attrWrapper.eq("attr_type", optional.get().getValue());
        if (catId != 0) {
            attrWrapper.eq("catelog_id", catId);
        }
        Object key = params.get("key");
        if (Objects.nonNull(key) && StringUtils.isNotBlank(String.valueOf(key))) {
            attrWrapper.and(e -> e.eq("attr_id", key).or().like("attr_name", key));
        }
        IPage<AttrEntity> result = this.page(page, attrWrapper);
        List<AttrEntity> records = result.getRecords();


        Set<Long> attrIds = records.stream().map(AttrEntity::getAttrId).collect(Collectors.toSet());
        List<AttrGroupEntity> attrGroups = Lists.newArrayList();
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelations = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(attrIds)) {
            QueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationWrapper = new QueryWrapper<>();
            attrAttrgroupRelationWrapper.in("attr_id", attrIds);
            attrAttrgroupRelations.addAll(attrAttrgroupRelationService.list(attrAttrgroupRelationWrapper));
            if (CollectionUtils.isNotEmpty(attrAttrgroupRelations)) {
                Set<Long> attrGroupIds = attrAttrgroupRelations.stream().map(AttrAttrgroupRelationEntity::getAttrGroupId).collect(Collectors.toSet());
                QueryWrapper<AttrGroupEntity> attrGroupWrapper = new QueryWrapper<>();
                attrGroupWrapper.in("attr_group_id", attrGroupIds);
                attrGroups.addAll(attrGroupService.list(attrGroupWrapper));
            }
        }

        Set<Long> catelogIds = records.stream().map(AttrEntity::getCatelogId).collect(Collectors.toSet());
        List<CategoryEntity> categorys = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(catelogIds)) {
            QueryWrapper<CategoryEntity> categoryWrapper = new QueryWrapper<>();
            categoryWrapper.in("cat_id", catelogIds);
            categorys.addAll(categoryService.list(categoryWrapper));
        }

        records.parallelStream().forEach(e -> {
            attrAttrgroupRelations.stream().filter(o -> Objects.equals(o.getAttrId(), e.getAttrId())).findAny().ifPresent(o
                    -> attrGroups.stream().filter(x -> Objects.equals(x.getAttrGroupId(), o.getAttrGroupId())).findAny().ifPresent(x
                    -> e.setAttrGroupName(x.getAttrGroupName())));
            categorys.stream().filter(o -> Objects.equals(o.getCatId(), e.getCatelogId())).findAny().ifPresent(o -> e.setCatelogName(o.getName()));
        });
        return new PageUtils(result);
    }

    @Override
    public AttrEntity getAttrById(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        List<Long> categoryIds = categoryService.findCategoryIds(attrEntity.getCatelogId());
        attrEntity.setCatelogIds(categoryIds);
        QueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationWrapper = new QueryWrapper<>();
        attrAttrgroupRelationWrapper.eq("attr_id", attrId);
        AttrAttrgroupRelationEntity attrAttrgroupRelation = attrAttrgroupRelationService.getOne(attrAttrgroupRelationWrapper);
        if (Objects.nonNull(attrAttrgroupRelation)) {
            attrEntity.setAttrGroupId(attrAttrgroupRelation.getAttrGroupId());
        }
        return attrEntity;
    }

    @Transactional
    @Override
    public boolean updateAndGroupById(AttrEntity attr) {
        AttrAttrgroupRelationEntity attrAttrgroupRelation = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelation.setAttrId(attr.getAttrId());
        attrAttrgroupRelation.setAttrGroupId(attr.getAttrGroupId());
        UpdateWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationWrapper = new UpdateWrapper<>();
        attrAttrgroupRelationWrapper.eq("attr_id", attr.getAttrId());
        attrAttrgroupRelationService.saveOrUpdate(attrAttrgroupRelation, attrAttrgroupRelationWrapper);
        return this.updateById(attr);
    }

    @Transactional
    @Override
    public boolean deleteAndGroup(Long[] attrIds) {
        List<Long> attrIdList = Lists.newArrayList(attrIds);
        if (CollectionUtils.isNotEmpty(attrIdList)) {
            QueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationWrapper = new QueryWrapper();
            attrAttrgroupRelationWrapper.in("attr_id", attrIdList);
            attrAttrgroupRelationService.remove(attrAttrgroupRelationWrapper);
        }
        return this.removeByIds(attrIdList);
    }

    @Override
    public List<ProductAttrValueEntity> listProductAttrBySpuId(Long spuId) {
        QueryWrapper<ProductAttrValueEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        return productAttrValueService.list(wrapper);
    }

    @Override
    public boolean updateProductAttrBySpuId(Long spuId, List<ProductAttrValueEntity> productAttrValues) {
        QueryWrapper<ProductAttrValueEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        productAttrValueService.remove(wrapper);
        productAttrValues.stream().forEach(e -> e.setSpuId(spuId));
        return productAttrValueService.saveBatch(productAttrValues);
    }
}
