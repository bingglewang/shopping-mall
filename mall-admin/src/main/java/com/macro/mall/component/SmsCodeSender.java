package com.macro.mall.component;

import com.macro.mall.domain.QueueEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 注册验证码的发出者(生产者)
 * Created by bingglewang on 2019/5/21.
 */
@Component
public class SmsCodeSender {
    private static Logger LOGGER =LoggerFactory.getLogger(SmsCodeSender.class);

    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendMessage(String phone,final long delayTimes) throws Exception{
        //给延迟队列发送消息
        amqpTemplate.convertAndSend(QueueEnum.QUEUE_SMS_CODE.getExchange(), QueueEnum.QUEUE_SMS_CODE.getRouteKey(), phone, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //给消息设置延迟毫秒值
                message.getMessageProperties().setExpiration(String.valueOf(delayTimes));
                return message;
            }
        });
        LOGGER.info("send注册手机号:{}",phone);
    }
}
