package com.atguigu.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizException;
import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.to.UserInfoTO;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.feign.ThirdParytFeignService;
import com.atguigu.gulimall.member.service.MemberLevelService;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.AccessTokenVO;
import com.atguigu.gulimall.member.vo.MemberLoginVO;
import com.atguigu.gulimall.member.vo.MemberRegistVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;

    @Autowired
    private ThirdParytFeignService thirdParytFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveUserInfo(MemberRegistVO memberRegistVO) {
        QueryWrapper<MemberLevelEntity> memberLevelQueryWrapper = new QueryWrapper<>();
        memberLevelQueryWrapper.eq("default_status", 1);
        MemberLevelEntity memberLevelEntity = memberLevelService.getOne(memberLevelQueryWrapper);
        Optional.ofNullable(memberLevelEntity).orElseThrow(() -> new BizException(BizExceptionEnum.M_GET_DATA_ERROR, "获取默认等级会员为空"));

        QueryWrapper<MemberEntity> memberEntityQueryWrapper = new QueryWrapper<>();
        memberEntityQueryWrapper.eq("username", memberRegistVO.getUsername());
        int usernameCount = this.count(memberEntityQueryWrapper);
        if (usernameCount > 0) {
            throw new BizException(BizExceptionEnum.M_CHECKED_DATA_UNQUALIFIED, "用户名已被注册");
        }

        memberEntityQueryWrapper.clear();
        memberEntityQueryWrapper.eq("mobile", memberRegistVO.getPhone());
        int mobileCount = this.count(memberEntityQueryWrapper);
        if (mobileCount > 0) {
            throw new BizException(BizExceptionEnum.M_CHECKED_DATA_UNQUALIFIED, "手机号已被注册");
        }

        //密码加密
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encryption = bCryptPasswordEncoder.encode(memberRegistVO.getPassword());

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUsername(memberRegistVO.getUsername());
        memberEntity.setPassword(encryption);
        memberEntity.setMobile(memberRegistVO.getPhone());
        memberEntity.setLevelId(memberLevelEntity.getId());
        this.save(memberEntity);
    }

    @Override
    public MemberEntity login(MemberLoginVO memberLoginVO) {
        QueryWrapper<MemberEntity> memberQueryWrapper = new QueryWrapper<>();
        memberQueryWrapper.eq("username", memberLoginVO.getLoginacct()).or().eq("mobile", memberLoginVO.getLoginacct());
        MemberEntity memberEntity = this.getOne(memberQueryWrapper);

        if (Objects.nonNull(memberEntity)) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            Boolean result = bCryptPasswordEncoder.matches(memberLoginVO.getPassword(), memberEntity.getPassword());
            if (result) {
                return memberEntity;
            }
        }
        throw new BizException(BizExceptionEnum.M_LOGIN_FAIL, "用户名或密码错误");
    }

    @Override
    public MemberEntity weiboLogin(AccessTokenVO accessTokenVO) {
        QueryWrapper<MemberEntity> memberQueryWrapper = new QueryWrapper<>();
        memberQueryWrapper.eq("social_uid", accessTokenVO.getUid());
        MemberEntity member = this.getOne(memberQueryWrapper);
        member = Optional.ofNullable(member).orElseGet(() -> {
            MemberEntity memberEntity = new MemberEntity();
            R r = thirdParytFeignService.getWeiboUserInfo(accessTokenVO.getAccessToken(), accessTokenVO.getUid());
            if (!Objects.equals(0, r.getCode())) {
                throw new BizException(BizExceptionEnum.M_REQ_REMOTESERVICE_FAIL, String.format("获取微博用户信息失败"));
            }
            UserInfoTO userInfoTO = JSON.parseObject(String.valueOf(r.get("data")), UserInfoTO.class);
            memberEntity.setNickname(userInfoTO.getName());
            memberEntity.setHeader(userInfoTO.getProfileImageUrl());
            memberEntity.setGender(Objects.equals("m", userInfoTO.getGender())? 1 : 0);
            return  memberEntity;
        });
        member.setSocialUid(accessTokenVO.getUid());
        member.setAccessToken(accessTokenVO.getAccessToken());
        member.setExpiresIn(accessTokenVO.getExpiresIn());
        this.saveOrUpdate(member);
        return member;
    }

}
