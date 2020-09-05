package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.vo.AccessTokenVO;
import com.atguigu.gulimall.member.vo.MemberLoginVO;
import com.atguigu.gulimall.member.vo.MemberRegistVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 18:36:17
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveUserInfo(MemberRegistVO memberRegistVO);

    MemberEntity login(MemberLoginVO memberLoginVO);

    MemberEntity weiboLogin(AccessTokenVO accessTokenVO);
}

