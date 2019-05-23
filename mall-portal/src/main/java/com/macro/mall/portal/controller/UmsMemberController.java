package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.component.SmsCodeSender;
import com.macro.mall.portal.dto.UmsMemberLoginParam;
import com.macro.mall.portal.dto.UmsMemberParam;
import com.macro.mall.portal.service.RedisService;
import com.macro.mall.portal.service.UmsMemberService;
import io.micrometer.core.instrument.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * 会员登录注册管理Controller
 * Created by macro on 2018/8/3.
 */
@Controller
@Api(tags = "UmsMemberController", description = "会员登录注册管理")
@RequestMapping("/sso")
public class UmsMemberController {
    @Autowired
    private UmsMemberService memberService;
    @Autowired
    private SmsCodeSender smsCodeSender;
    @Autowired
    private RedisService redisService;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @ApiOperation(value = "商城用户注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<UmsMember> register(@RequestBody UmsMemberParam umsMemberParam, @RequestParam(value = "code") String code, BindingResult result) {
        String redisCode = redisService.get(umsMemberParam.getUsername());
        if(StringUtils.isBlank(code) || !code.equals(redisCode)){
            return  CommonResult.failed("验证码错误");
        }
        UmsMember umsMember = memberService.register(umsMemberParam);
        if (umsMember == null) {
            return  CommonResult.failed("手机号已经被注册");
        }
        return CommonResult.success(umsMember);
    }

    @ApiOperation(value = "发送验证码")
    @RequestMapping(value = "/sendCode",method = RequestMethod.POST)
    @ResponseBody
    public CommonResult sendCode(@RequestParam(value = "phone") String phone){
        String msg = "短信发送成功";
        if (!phone.matches("^1[3|4|5|7|8][0-9]{9}$")) {
            msg = "非法手机号";
            return CommonResult.failed(msg);
        }
        try {
            smsCodeSender.sendMessage(phone,100);
        }catch (Exception e){
            return CommonResult.failed(e.getMessage());
        }
        return CommonResult.success(null,msg);
    }

    @ApiOperation(value = "登录以后返回token")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult login(@RequestBody UmsMemberLoginParam umsMemberLoginParam, BindingResult result) {
        UmsMember umsMember = memberService.getByUsername(umsMemberLoginParam.getUsername());
        if(umsMember == null){
            return CommonResult.validateFailed("该手机号还未注册");
        }
        String token = memberService.login(umsMemberLoginParam.getUsername(), umsMemberLoginParam.getPassword());
        if (token == null) {
            return CommonResult.validateFailed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        tokenMap.put("id",umsMember.getId().toString());
        tokenMap.put("mobile",umsMember.getPhone());
        tokenMap.put("nickname",umsMember.getNickname());//昵称
        tokenMap.put("portrait",umsMember.getIcon());//头像
        return CommonResult.success(tokenMap);
    }


    @ApiOperation(value = "密码重置")
    @RequestMapping(value = "/resetPwd", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult resetPwd(@RequestBody UmsMemberParam umsMemberParam, @RequestParam(value = "code") String code,  BindingResult result) {
        String redisCode = redisService.get(umsMemberParam.getUsername());
        if(StringUtils.isBlank(code) || !code.equals(redisCode)){
            return  CommonResult.failed("验证码错误");
        }
        int resetResult = memberService.reset(umsMemberParam);
        if (resetResult == -1) {
            return  CommonResult.failed("手机号不存在");
        }else if(resetResult == -2){
            return  CommonResult.failed("密码重置失败");
        }
        return CommonResult.success("密码重置成功");
    }

    @ApiOperation(value = "刷新token")
    @RequestMapping(value = "/token/refresh", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult refreshToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        String refreshToken = memberService.refreshToken(token);
        if (refreshToken == null) {
            return CommonResult.failed();
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", refreshToken);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }

    @ApiOperation(value = "获取当前登录用户信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UmsMember> getMemberInfo() {
        UmsMember memberUserDetails = (UmsMember) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        UmsMember umsMember = memberService.getByUsername(memberUserDetails.getUsername());
        return CommonResult.success(umsMember);
    }

    @ApiOperation(value = "登出功能")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult logout() {
        return CommonResult.success(null);
    }

    @ApiOperation("获取指定用户信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UmsMember> getItem(@PathVariable Long id) {
        UmsMember Member = memberService.getById(id);
        return CommonResult.success(Member);
    }

    @ApiOperation("修改指定用户信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable Long id, @RequestBody UmsMember Member) {
        int count = memberService.update(id, Member);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
