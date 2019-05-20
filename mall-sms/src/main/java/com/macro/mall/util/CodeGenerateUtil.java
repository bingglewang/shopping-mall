package com.macro.mall.util;

import com.macro.mall.dto.SmsCodeProperties;

import java.util.Random;

public class CodeGenerateUtil {
    /**
     * 随机生成6位数的短信码
     * @param phone
     * @return
     */
    public static SmsCodeProperties generateCode(String phone) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int next = random.nextInt(10);
            code.append(next);
        }
        return new SmsCodeProperties(phone, code.toString(), new java.util.Date());
    }
}
