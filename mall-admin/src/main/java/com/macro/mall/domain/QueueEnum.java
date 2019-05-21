package com.macro.mall.domain;

import lombok.Getter;

/**
 * 消息队列枚举配置
 * Created by bingglewang on 2019/5/21.
 */
@Getter
public enum QueueEnum {
    /**
     * 消息通知队列
     */
    QUEUE_SMS_CODE("mall.code.direct", "mall.code.register", "mall.code.register");

    /**
     * 交换名称
     */
    private String exchange;
    /**
     * 队列名称
     */
    private String name;
    /**
     * 路由键
     */
    private String routeKey;

    QueueEnum(String exchange, String name, String routeKey) {
        this.exchange = exchange;
        this.name = name;
        this.routeKey = routeKey;
    }
}
