package com.macro.mall.service.impl;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.config.SMSConfig;
import com.macro.mall.dto.SmsCodeProperties;
import com.macro.mall.service.RedisService;
import com.macro.mall.service.SmsTengxunMegService;
import com.macro.mall.util.CodeGenerateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SmsTengxunMegServiceImpl implements SmsTengxunMegService {

    @Autowired
    private SMSConfig smsConfig;

    @Autowired
    private RedisService redisService;

    @Override
    public boolean sendMessageByTemplate(String phone) throws Exception {
        return false;
    }

    @Override
    public CommonResult sendMessageModel(String phone) {
        String msg = "短信发送成功";
        if (!phone.matches("^1[3|4|5|7|8][0-9]{9}$")) {
            msg = "非法手机号";
            return CommonResult.failed(msg);
        }
        try {
            //制作验证码，6位随机数字
            SmsCodeProperties sms = CodeGenerateUtil.generateCode(phone);
            //{参数}
            String[] params = {sms.getCode()};
            SmsSingleSender ssender = new SmsSingleSender(Integer.valueOf(smsConfig.getAppId()), smsConfig.getAppKey());
            // 签名参数未提供或者为空时，会使用默认签名发送短信
            SmsSingleSenderResult result = ssender.sendWithParam("86", phone,
                    Integer.valueOf(smsConfig.getTemplateId()), params, smsConfig.getSignName(), "", "");
            //0代表成功 非0代表失败
            if (result != null && "OK".equals(result.errMsg)) {
                // 将验证码保存到redis，并且设置过期时间
                redisService.set(phone, sms.getCode());
                redisService.expire(phone,120);
            }else{
                return CommonResult.failed(result.errMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed(e.getMessage());
        }
        return CommonResult.success(null,msg);
    }
}
