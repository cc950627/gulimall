package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.atguigu.gulimall.ware.service.WareInfoService;
import com.atguigu.gulimall.ware.vo.MemberAddressVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> iPage = new Query<WareInfoEntity>().getPage(params);
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        Object key = params.get("key");
        if (Objects.nonNull(key) && StringUtils.isNotBlank(String.valueOf(key))) {
            wrapper.and(e -> e.eq("id", key).or().like("name", key).or().like("address", key).or().like("areacode", key));
        }
        IPage<WareInfoEntity> page = this.page(iPage, wrapper);
        return new PageUtils(page);
    }

    @Override
    public MemberAddressVO getFare(Long addrId) {
        R r = memberFeignService.info(addrId);
        MemberAddressVO addressVO = JSON.parseObject(JSON.toJSONString(r.get("memberReceiveAddress")), MemberAddressVO.class);
        String phone = Optional.ofNullable(addressVO.getPhone()).orElse("0");
        addressVO.setFare(new BigDecimal(StringUtils.substring(phone, phone.length() - 1)));
        return addressVO;
    }

}
