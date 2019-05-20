package com.macro.mall.dto;

import java.util.Date;

public class SmsCodeProperties {
    private String phone; //要发送的电话号码
    private String code; //发送的验证码内容
    private Date date; //发送时间

    public SmsCodeProperties(String phone, String code, Date date) {
        this.phone = phone;
        this.code = code;
        this.date = date;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
