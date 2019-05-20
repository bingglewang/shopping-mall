package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.service.SmsTengxunMegService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "SmsController", description = "短信发送接口")
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsTengxunMegService smsTengxunMegService;

    @RequestMapping("/sendCode")
    @ApiOperation(value = "指定模板短信发送接口",httpMethod = "POST")
    @ResponseBody
    public CommonResult sendCode(@RequestParam(value = "phone") String phone){
        return smsTengxunMegService.sendMessageModel(phone);
    }
}
