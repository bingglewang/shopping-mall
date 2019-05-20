package com.macro.mall.service;

import com.macro.mall.common.api.CommonResult;

public interface SmsTengxunMegService {
    /**
     * 自定义模板发送
     */
    boolean sendMessageByTemplate(String phone) throws Exception;

    /**
     * 指定模板ＩＤ发送短信
     * @return OK 成功  null 失败
     * Resp 是自定义的返回json
     */
    CommonResult sendMessageModel(String phone);

}
