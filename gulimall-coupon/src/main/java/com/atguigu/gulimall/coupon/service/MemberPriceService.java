package com.atguigu.gulimall.coupon.service;

import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;

import java.util.Map;

/**
 * 商品会员价格
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 18:13:17
 */
public interface MemberPriceService extends IService<MemberPriceEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

