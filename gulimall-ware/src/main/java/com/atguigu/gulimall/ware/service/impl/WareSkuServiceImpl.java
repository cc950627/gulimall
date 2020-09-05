package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.SkuInfoTO;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.to.mq.StockLockDetailTO;
import com.atguigu.common.to.mq.StockLockTO;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.gulimall.ware.constant.PurchaseStatusEnum;
import com.atguigu.gulimall.ware.constant.WareLockStatusEnum;
import com.atguigu.gulimall.ware.dao.WareSkuDao;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.atguigu.gulimall.ware.entity.WareOrderTaskEntity;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.atguigu.gulimall.ware.feign.ProductFeignService;
import com.atguigu.gulimall.ware.feign.SearchFeignService;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskDetailService;
import com.atguigu.gulimall.ware.service.WareOrderTaskService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.atguigu.gulimall.ware.vo.OrderItemVO;
import com.atguigu.gulimall.ware.vo.PurchaseDetailVO;
import com.atguigu.gulimall.ware.vo.WareSkuLockVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean addStock(List<PurchaseDetailVO> purchaseDetailVOS) {
        Set<Long> purchaseDetailIds = purchaseDetailVOS.stream().map(PurchaseDetailVO::getItemId).collect(Collectors.toSet());
        List<PurchaseDetailEntity> purchaseDetails = purchaseDetailService.listByIds(purchaseDetailIds);
        if (CollectionUtils.isEmpty(purchaseDetails)) {
            throw new BizException(BizExceptionEnum.W_REQ_REMOTESEFINISH_FAIL, String.format("采购需求ID：%s不存在", purchaseDetailIds));
        }
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        Map<Long, Set<Long>> map = purchaseDetails.stream().filter(e -> Objects.equals(e.getStatus(), PurchaseStatusEnum.FINISH.getStatus()))
                .collect(Collectors.groupingBy(PurchaseDetailEntity::getWareId, Collectors.mapping(PurchaseDetailEntity::getSkuId, Collectors.toSet())));
        map.forEach((k, v) -> wrapper.or(o -> o.eq("ware_id", k).in("sku_id", v)));
        List<WareSkuEntity> wareSkus = this.list(wrapper);

        Set<Long> skuIds = map.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        R r = productFeignService.listSkuInfoBySkuIds(skuIds);
        if (!Objects.equals(r.getCode(), 0)) {
            throw new BizException(BizExceptionEnum.W_REQ_REMOTESERVICE_FAIL, String.format("server-name: gulimall-product，url：/product/skuinfo/listBySkuIds，param：%s", skuIds.toString()));
        }
        List<SkuInfoTO> skuInfoTOS = JSON.parseArray(JSON.toJSONString(r.get("data")), SkuInfoTO.class);

        List<WareSkuEntity> wareSkuList = Lists.newArrayList();
        purchaseDetails.stream().forEach(e -> {
            Optional<WareSkuEntity> optional = wareSkus.stream().filter(o -> Objects.equals(o.getSkuId(), e.getSkuId()) && Objects.equals(o.getWareId(), e.getWareId())).findAny();
            optional.ifPresent(o -> o.setStock(o.getStock() + e.getSkuNum()));
            WareSkuEntity wareSku = optional.orElseGet(() -> {
                WareSkuEntity wareSkuEntity = new WareSkuEntity();
                wareSkuEntity.setSkuId(e.getSkuId());
                wareSkuEntity.setWareId(e.getWareId());
                wareSkuEntity.setStock(e.getSkuNum());
                Optional<SkuInfoTO> skuOptional = skuInfoTOS.stream().filter(o -> Objects.equals(o.getSkuId(), e.getSkuId())).findAny();
                skuOptional.ifPresent(o -> wareSkuEntity.setSkuName(o.getSkuName()));
                return wareSkuEntity;
            });
            wareSkuList.add(wareSku);
        });

        // 更新es
        List<SkuEsModel> skuEsModels = wareSkuList.stream().map(e -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            skuEsModel.setSkuId(e.getSkuId());
            skuEsModel.setHasStock(e.getStock() > 0);
            return skuEsModel;
        }).collect(Collectors.toList());
        R r2 = searchFeignService.updateHasStock(skuEsModels);
        if (!Objects.equals(r2.getCode(), 0)) {
            throw new BizException(BizExceptionEnum.W_REQ_REMOTESERVICE_FAIL, String.format("server-name: gulimall-search，url：/search/es/product/update，param：%s", skuEsModels.toString()));
        }

        return this.saveOrUpdateBatch(wareSkuList);
    }

    @Override
    public Map<Long, Integer> getSkuStock(List<Long> skuIds) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        wrapper.in("sku_id", skuIds);
        List<WareSkuEntity> wareSkus = this.list(wrapper);
        Map<Long, Integer> map = wareSkus.stream().collect(Collectors.groupingBy(WareSkuEntity::getSkuId, Collectors.summingInt(e -> e.getStock() - e.getStockLocked())));
        skuIds.stream().forEach(e -> map.putIfAbsent(e, 0));
        return map;
    }

    @Transactional
    @Override
    public void orderLockStock(WareSkuLockVO wareSkuLockVO) {
        WareOrderTaskEntity wareOrderTask = new WareOrderTaskEntity();
        wareOrderTask.setOrderSn(wareSkuLockVO.getOrder());
        wareOrderTaskService.save(wareOrderTask);

        Set<Long> skuIds = wareSkuLockVO.getOrderItems().stream().map(OrderItemVO::getSkuId).collect(Collectors.toSet());
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("sku_id", skuIds);
        queryWrapper.and(e -> e.last("stock > stock_locked for update"));
        List<WareSkuEntity> wareInfos = this.list(queryWrapper);
        Map<Long, List<WareSkuEntity>> skuWare = wareInfos.stream().collect(Collectors.groupingBy(WareSkuEntity::getSkuId));

        List<String> skuTitlelist = wareSkuLockVO.getOrderItems().stream().filter(e -> !skuWare.containsKey(e.getSkuId()))
                .map(OrderItemVO::getSkuTitle).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(skuTitlelist)) {
            throw new BizException(BizExceptionEnum.W_LOCK_STOCK_FAIL, String.format("商品：%s无货", skuTitlelist.toString()));
        }
        skuTitlelist = wareSkuLockVO.getOrderItems().stream().filter(e -> skuWare.get(e.getSkuId()).stream().allMatch(
                o -> o.getStock() - o.getStockLocked() < e.getCount())).map(OrderItemVO::getSkuTitle).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(skuTitlelist)) {
            throw new BizException(BizExceptionEnum.W_LOCK_STOCK_FAIL, String.format("商品：%s库存不足", skuTitlelist.toString()));
        }

        List<WareSkuEntity> updateWareSku = wareSkuLockVO.getOrderItems().stream().map(e -> {
            Optional<WareSkuEntity> optional = skuWare.get(e.getSkuId()).stream().filter(o -> o.getStock() - o.getStockLocked() >= e.getCount()).findAny();
            optional.ifPresent(o -> o.setStockLocked(o.getStockLocked() + e.getCount()));
            return optional.get();
        }).collect(Collectors.toList());
        this.updateBatchById(updateWareSku);

        List<WareOrderTaskDetailEntity> wareOrderTaskDetails = wareSkuLockVO.getOrderItems().stream().map(e -> {
            WareOrderTaskDetailEntity wareOrderTaskDetail = new WareOrderTaskDetailEntity();
            wareOrderTaskDetail.setSkuId(e.getSkuId());
            wareOrderTaskDetail.setSkuName(e.getSkuTitle());
            wareOrderTaskDetail.setSkuNum(e.getCount());
            wareOrderTaskDetail.setTaskId(wareOrderTask.getId());
            wareOrderTaskDetail.setLockStatus(WareLockStatusEnum.ISLOCK.getStatus());
            updateWareSku.stream().filter(o -> Objects.equals(e.getSkuId(), o.getSkuId())).findAny().ifPresent(o -> wareOrderTaskDetail.setWareId(o.getWareId()));
            return wareOrderTaskDetail;
        }).collect(Collectors.toList());
        wareOrderTaskDetailService.saveBatch(wareOrderTaskDetails);

        StockLockTO stockLockTO = new StockLockTO();
        stockLockTO.setId(wareOrderTask.getId());
        List<StockLockDetailTO> stockLockDetailTOS = wareOrderTaskDetails.stream().map(e -> {
            StockLockDetailTO stockLockDetailTO = new StockLockDetailTO();
            BeanUtils.copyProperties(e, stockLockDetailTO);
            return stockLockDetailTO;
        }).collect(Collectors.toList());
        stockLockTO.setStockLockDetails(stockLockDetailTOS);
        rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockTO);
    }

}
