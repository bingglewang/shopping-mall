package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.component.SmsCodeSender;
import com.macro.mall.dto.UmsMemberLoginParam;
import com.macro.mall.dto.UmsMemberParam;
import com.macro.mall.model.UmsMember;
import com.macro.mall.service.RedisService;
import com.macro.mall.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城用户管理
 * Created by bingglewang on 2019/5/22.
 */
@Controller
@Api(tags = "UmsMemberController", description = "商城用户管理")
@RequestMapping("/member")
public class UmsMemberController{
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
    public CommonResult<UmsMember> register(@RequestBody UmsMemberParam umsMemberParam, @RequestParam(value = "code") String code,  BindingResult result) {
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
        UmsMember umsMember = memberService.getMemberByUsername(umsMemberLoginParam.getUsername());
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
    public CommonResult getMemberInfo(Principal principal) {
        String username = principal.getName();
        UmsMember umsMember = memberService.getMemberByUsername(username);
        Map<String, Object> data = new HashMap<>();
        data.put("username", umsMember.getUsername());
        data.put("roles", new String[]{"TEST"});
        data.put("icon", umsMember.getIcon());
        return CommonResult.success(data);
    }

    @ApiOperation(value = "登出功能")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult logout() {
        return CommonResult.success(null);
    }

    @ApiOperation("根据用户名或姓名分页获取用户列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<UmsMember>> list(@RequestParam(value = "name", required = false) String name,
                                                   @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<UmsMember> MemberList = memberService.list(name, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(MemberList));
    }

    @ApiOperation("获取指定用户信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UmsMember> getItem(@PathVariable Long id) {
        UmsMember Member = memberService.getItem(id);
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

    @ApiOperation("删除指定用户信息")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@PathVariable Long id) {
        int count = memberService.delete(id);
        if (count > 0) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }
}
