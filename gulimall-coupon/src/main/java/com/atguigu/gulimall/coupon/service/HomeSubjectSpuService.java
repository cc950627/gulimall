package com.atguigu.gulimall.coupon.service;

import com.atguigu.gulimall.coupon.entity.HomeSubjectSpuEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;

import java.util.Map;

/**
 * 专题商品
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 18:13:17
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

