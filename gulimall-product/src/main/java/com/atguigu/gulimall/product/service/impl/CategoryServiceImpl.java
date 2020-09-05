package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.utils.Constant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zengtengpeng.annotation.Lock;
import com.zengtengpeng.enums.LockModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    private static final String LOCK_NAME = "lock-catagory";

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Lock(keys = LOCK_NAME, lockModel = LockModel.READ, lockWatchdogTimeout = 3000, attemptTimeout = 3000)
    public List<CategoryEntity> listWithTreeRedisLock() {
        RBucket<String> bucket = redissonClient.getBucket(Constant.REDIS_CATEGORY_KEY);
        String categoryJson = bucket.get();
        if (StringUtils.isBlank(categoryJson)) {
            List<CategoryEntity> categoryTree = this.listWithTree();
            bucket.set(JSON.toJSONString(categoryTree), 1, TimeUnit.DAYS);
            return categoryTree;
        }
        return JSON.parseArray(categoryJson, CategoryEntity.class);
    }

    @Override
    @Transactional
    @Lock(keys = LOCK_NAME, lockModel = LockModel.WRITE, lockWatchdogTimeout = 3000, attemptTimeout = 3000)
    public boolean removeMeunByIds(List<Long> catIds) {
        if (CollectionUtils.isEmpty(catIds)) {
            return false;
        }
        baseMapper.deleteBatchIds(catIds);
        this.updateRedisCache();
        return true;
    }

    @Override
    public List<Long> findCategoryIds(Long catId) {
        List<Long> categoryIds = Lists.newArrayList();
        findParentById(catId, categoryIds);
        return categoryIds;
    }

    @Override
    @Transactional
    @Lock(keys = LOCK_NAME, lockModel = LockModel.WRITE, lockWatchdogTimeout = 3000, attemptTimeout = 3000)
    public boolean updateDetailById(CategoryEntity category) {
        if (StringUtils.isNotBlank(category.getName())) {
            CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
            categoryBrandRelationEntity.setCatelogId(category.getCatId());
            categoryBrandRelationEntity.setCatelogName(category.getName());
            categoryBrandRelationService.updateById(categoryBrandRelationEntity);
        }
        boolean reuslt = this.updateById(category);
        this.updateRedisCache();
        return reuslt;
    }

    @Override
    @Transactional
    @Lock(keys = LOCK_NAME, lockModel = LockModel.WRITE, lockWatchdogTimeout = 3000, attemptTimeout = 3000)
    public boolean deteleMeunByIds(List<Long> catIds) {
        if (CollectionUtils.isNotEmpty(catIds)) {
            QueryWrapper<CategoryBrandRelationEntity> categoryEntityWrapper = new QueryWrapper<>();
            categoryEntityWrapper.in("catelog_id", catIds);
            List<CategoryBrandRelationEntity> attrAttrgroupRelations = categoryBrandRelationService.list(categoryEntityWrapper);
            if (CollectionUtils.isNotEmpty(attrAttrgroupRelations)) {
                throw new BizException(BizExceptionEnum.P_REMOVE_ATTRGROUP_FAIL, "产品分类被引用，不允许删除");
            }
        }
        boolean reuslt = this.removeByIds(catIds);
        this.updateRedisCache();
        return reuslt;
    }

    @Override
    public List<CategoryEntity> listByParentCid(Long parentCid) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_cid", parentCid);
        return this.list(wrapper);
    }

    @Override
    public Map<Long, List<CategoryEntity>> showCatalogs() {
        List<CategoryEntity> categorys = this.listWithTreeRedisLock();
        Map<Long, List<CategoryEntity>> result = categorys.stream().collect(Collectors.toMap(CategoryEntity::getCatId, CategoryEntity::getChildes));
        return result;
    }

    private List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categories = baseMapper.selectList(null);
        Map<Boolean, List<CategoryEntity>> categoryMap = categories.stream().collect(Collectors.partitioningBy(e -> Objects.equals(e.getParentCid().intValue(), 0)));
        return getChildes(categoryMap, new AtomicReference<>());
    }

    private void findParentById(Long catId, List<Long> categoryIds) {
        CategoryEntity categoryEntity = this.getById(catId);
        Optional.ofNullable(categoryEntity).ifPresent(e -> {
            findParentById(categoryEntity.getParentCid(), categoryIds);
            categoryIds.add(categoryEntity.getCatId());
        });
    }

    private static List<CategoryEntity> getChildes(Map<Boolean, List<CategoryEntity>> categoryMap, AtomicReference<List<CategoryEntity>> atomic) {
        List<CategoryEntity> rootCategories = categoryMap.getOrDefault(true, Lists.newArrayList());
        List<CategoryEntity> otherCategories = categoryMap.getOrDefault(false, Lists.newArrayList());
        atomic.set(otherCategories);
        rootCategories = rootCategories.stream().sorted(Comparator.comparingInt(CategoryEntity::getSort)).map(e -> {
            Map<Boolean, List<CategoryEntity>> partition = atomic.get().stream().collect(Collectors.partitioningBy(o
                    -> Objects.equals(o.getParentCid(), e.getCatId())));
            List<CategoryEntity> childCategories = getChildes(partition, atomic);
            e.setChildes(childCategories);
            return e;
        }).collect(Collectors.toList());
        return rootCategories;
    }

    private void updateRedisCache() {
        RBucket<Object> bucket = redissonClient.getBucket(Constant.REDIS_CATEGORY_KEY);
        bucket.delete();
        List<CategoryEntity> categorys = this.listWithTreeRedisLock();
        bucket.set(JSON.toJSONString(categorys), 1, TimeUnit.DAYS);
    }

}
