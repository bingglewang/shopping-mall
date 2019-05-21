package com.macro.mall.component;

import com.macro.mall.service.SmsTengxunMegService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 注册验证码消息的处理者(消费者)
 * Created by bingglewang on 2019/5/21.
 */
@Component
@RabbitListener(queues = "mall.code.register")
public class SmsCodeReceiver {
    private static Logger LOGGER =LoggerFactory.getLogger(SmsCodeReceiver.class);

    @Autowired
    private SmsTengxunMegService smsTengxunMegService;

    @RabbitHandler
    public void handle(String phone){
        smsTengxunMegService.sendMessageModel(phone);
        LOGGER.info("要发送验证码的手机号:{}",phone);
    }
}
