package com.atguigu.gulimall.member.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.feign.ThirdParytFeignService;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.gulimall.member.vo.AccessTokenVO;
import com.atguigu.gulimall.member.vo.MemberLoginVO;
import com.atguigu.gulimall.member.vo.MemberRegistVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 18:36:17
 */
@RestController
@RequestMapping("member/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/saveUserInfo")
    public R saveUserInfo(@RequestBody MemberRegistVO memberRegistVO) {
        memberService.saveUserInfo(memberRegistVO);
        return R.ok();
    }

    @PostMapping("/login")
    public  R login(@RequestBody MemberLoginVO memberLoginVO) {
        MemberEntity memberEntity = memberService.login(memberLoginVO);
        return R.ok().put("data", memberEntity);
    }

    @GetMapping("/weiboLogin")
    public R weiboLogin(@RequestParam("accessToken") String accessToken) {
        MemberEntity memberEntity = memberService.weiboLogin(JSON.parseObject(accessToken, AccessTokenVO.class));
        return R.ok().put("data", memberEntity);
    }

}
