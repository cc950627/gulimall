package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        IPage<AttrGroupEntity> page = new Query().getPage(params);
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper();
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        Object key = params.get("key");
        if (Objects.nonNull(key) && StringUtils.isNotBlank(String.valueOf(key))) {
            queryWrapper.and(e -> e.eq("attr_group_id", key).or().like("attr_group_name", key));
        }
        IPage<AttrGroupEntity> result = this.page(page, queryWrapper);

        List<AttrGroupEntity> records = result.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            Set<Long> catelogIds = records.stream().map(AttrGroupEntity::getCatelogId).collect(Collectors.toSet());
            List<CategoryEntity> categoryEntities = categoryService.listByIds(catelogIds);
            records.parallelStream().forEach(e -> categoryEntities.parallelStream().filter(o
                    -> Objects.equals(e.getCatelogId(), o.getCatId())).findAny().ifPresent(o -> e.setCatelogName(o.getName())));
        }
        return new PageUtils(result);
    }

    @Override
    public boolean deleteByIds(List<Long> attrGroupIds) {
        if (CollectionUtils.isNotEmpty(attrGroupIds)) {
            QueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationWrapper = new QueryWrapper<>();
            attrAttrgroupRelationWrapper.in("attr_group_id", attrGroupIds);
            List<AttrAttrgroupRelationEntity> attrAttrgroupRelations = attrAttrgroupRelationService.list(attrAttrgroupRelationWrapper);
            if (CollectionUtils.isNotEmpty(attrAttrgroupRelations)) {
                throw new BizException(BizExceptionEnum.P_REMOVE_ATTRGROUP_FAIL, "属性分组被引用，不允许删除");
            }
        }
        return this.removeByIds(attrGroupIds);
    }

    @Override
    public List<AttrEntity> listAttrGroupByAttrGroupId(Long attrGroupId) {
        QueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationWrapper = new QueryWrapper<>();
        attrAttrgroupRelationWrapper.eq("attr_group_id", attrGroupId);
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelations = attrAttrgroupRelationService.list(attrAttrgroupRelationWrapper);
        Set<Long> attrId = attrAttrgroupRelations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(attrId)) {
            return attrService.listByIds(attrId);
        }
        return Collections.emptyList();
    }

    @Override
    public PageUtils listAttrByAttrGroupId(Long attrGroupId, Map<String, Object> params) {
        AttrGroupEntity group = this.getById(attrGroupId);
        Optional.ofNullable(group).orElseThrow(() -> new BizException(BizExceptionEnum.P_GET_ATTRRELATION_FAIL,String.format("无效的属性分组ID：%s", attrGroupId)));

        QueryWrapper<AttrGroupEntity> attrGroupWrapper = new QueryWrapper<>();
        attrGroupWrapper.eq("catelog_id", group.getCatelogId());
        List<AttrGroupEntity> attrGroups = this.list(attrGroupWrapper);
        Set<Long> attrGroupIds = attrGroups.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toSet());

        QueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationWrapper = new QueryWrapper<>();
        attrAttrgroupRelationWrapper.in("attr_group_id", attrGroupIds);
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelations = attrAttrgroupRelationService.list(attrAttrgroupRelationWrapper);

        IPage<AttrEntity> page = new Query<AttrEntity>().getPage(params);
        QueryWrapper<AttrEntity> attrWrapper = new QueryWrapper<>();
        attrWrapper.eq("catelog_id", group.getCatelogId());
        if (CollectionUtils.isNotEmpty(attrAttrgroupRelations)) {
            Set<Long> attrIds = attrAttrgroupRelations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toSet());
            attrWrapper.notIn("attr_id", attrIds);
        }
        Object key = params.get("key");
        if (Objects.nonNull(key) && StringUtils.isNotBlank(String.valueOf(key))) {
            attrWrapper.and(e -> e.eq("attr_id", key).or().like("attr_name", key));
        }
        IPage<AttrEntity> pageAttr = attrService.page(page, attrWrapper);
        return new PageUtils(pageAttr);
    }

    @Override
    public boolean saveAttrRelation(List<AttrAttrgroupRelationEntity> attrAttrgroupRelations) {
        if (CollectionUtils.isEmpty(attrAttrgroupRelations)) {
            throw new BizException(BizExceptionEnum.P_SAVE_ATTRRELATION_FAIL, "属性分组关联的属性不能为空");
        }
        attrAttrgroupRelations.stream().forEachOrdered(e -> attrAttrgroupRelationService.saveOrUpdate(e,
                new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", e.getAttrGroupId()).eq("attr_id", e.getAttrId())));
        return true;
    }

    @Override
    public List<AttrGroupEntity> withattrByCatelogId(Long catelogId) {
        QueryWrapper<AttrGroupEntity> attrGroupWrapper = new QueryWrapper<>();
        attrGroupWrapper.eq("catelog_id", catelogId);
        List<AttrGroupEntity> attrGroups = this.list(attrGroupWrapper);
        if (CollectionUtils.isEmpty(attrGroups)) {
            return Collections.emptyList();
        }

        Set<Long> attrGroupIds = attrGroups.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toSet());
        QueryWrapper<AttrAttrgroupRelationEntity> attrAttrgroupRelationWrapper = new QueryWrapper<>();
        attrAttrgroupRelationWrapper.in("attr_group_id", attrGroupIds);
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelations = attrAttrgroupRelationService.list(attrAttrgroupRelationWrapper);
        if (CollectionUtils.isEmpty(attrAttrgroupRelations)) {
            return attrGroups;
        }

        Set<Long> attrIds = attrAttrgroupRelations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toSet());
        QueryWrapper<AttrEntity> attrWrapper = new QueryWrapper<>();
        attrWrapper.in("attr_id", attrIds);
        List<AttrEntity> attrs = attrService.list(attrWrapper);

        Map<Long, Set<Long>> map = attrAttrgroupRelations.stream().collect(Collectors.groupingBy(
                AttrAttrgroupRelationEntity::getAttrGroupId, Collectors.mapping(AttrAttrgroupRelationEntity::getAttrId, Collectors.toSet())));
        attrGroups.parallelStream().forEach(e -> e.setAttrs(attrs.stream().filter(o
                -> map.getOrDefault(e.getAttrGroupId(), Sets.newHashSet()).contains(o.getAttrId())).collect(Collectors.toList())));
        return attrGroups;
    }

}
