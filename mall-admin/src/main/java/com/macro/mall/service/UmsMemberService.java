package com.macro.mall.service;

import com.macro.mall.dto.UmsMemberParam;
import com.macro.mall.model.UmsMember;

import java.util.List;

/**
 * 商城会员管理
 * Created by bingglewang on 2019/5/22.
 */
public interface UmsMemberService {
    /**
     * 根据用户名获取后台管理员
     */
    UmsMember getMemberByUsername(String username);

    /**
     * 注册功能
     */
    UmsMember register(UmsMemberParam umsMemberParam);

    /**
     * 重置密码
     * @param umsMemberParam
     * @return
     */
    int reset(UmsMemberParam umsMemberParam);

    /**
     * 登录功能
     * @param username 用户名
     * @param password 密码
     * @return 生成的JWT的token
     */
    String login(String username,String password);

    /**
     * 刷新token的功能
     * @param oldToken 旧的token
     */
    String refreshToken(String oldToken);

    /**
     * 根据用户id获取用户
     */
    UmsMember getItem(Long id);

    /**
     * 根据用户名或昵称分页查询用户
     */
    List<UmsMember> list(String name, Integer pageSize, Integer pageNum);

    /**
     * 修改指定用户信息
     */
    int update(Long id, UmsMember member);

    /**
     * 删除指定用户
     */
    int delete(Long id);
}
