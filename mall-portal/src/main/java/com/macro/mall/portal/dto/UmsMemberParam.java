package com.macro.mall.portal.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Date;

/**
 * 商城app用户登录参数
 * Created by bingglewang on 2019/5/22.
 */
@Getter
@Setter
public class UmsMemberParam {
    @ApiModelProperty(value = "用户名", required = true)
    @NotEmpty(message = "用户名不能为空")
    private String username;
    @ApiModelProperty(value = "密码", required = true)
    @NotEmpty(message = "密码不能为空")
    private String password;
    @ApiModelProperty(value = "用户头像")
    private String icon;
   /* @ApiModelProperty(value = "邮箱")
    @Email(message = "邮箱格式不合法")*/
    private String email;
    @ApiModelProperty(value = "用户昵称")
    private String nickName;
    @ApiModelProperty(value = "手机号码")
    private String phone;
    @ApiModelProperty(value = "性别：0->未知；1->男；2->女")
    private Integer gender;
    @ApiModelProperty(value = "生日")
    private Date birthday;
    @ApiModelProperty(value = "所做城市")
    private String city;
    @ApiModelProperty(value = "职业")
    private String job;
    @ApiModelProperty(value = "个性签名")
    private String personalizedSignature;
}
