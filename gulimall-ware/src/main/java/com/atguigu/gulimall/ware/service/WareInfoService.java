package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.vo.MemberAddressVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 19:07:49
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    MemberAddressVO getFare(Long addrId);

}

