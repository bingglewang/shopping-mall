package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.dto.UmsMemberParam;
import com.macro.mall.mapper.UmsMemberLoginLogMapper;
import com.macro.mall.mapper.UmsMemberMapper;
import com.macro.mall.model.*;
import com.macro.mall.service.UmsMemberService;
import com.macro.mall.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Service
public class UmsMemberServiceImpl implements UmsMemberService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UmsMemberServiceImpl.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Autowired
    private UmsMemberMapper memberMapper;
    @Autowired
    private UmsMemberLoginLogMapper loginLogMapper;


    @Override
    public UmsMember getMemberByUsername(String username) {
        UmsMemberExample example = new UmsMemberExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<UmsMember> memberList = memberMapper.selectByExample(example);
        if (memberList != null && memberList.size() > 0) {
            return memberList.get(0);
        }
        return null;
    }

    @Override
    public UmsMember register(UmsMemberParam umsMemberParam) {
        UmsMember umsMember = new UmsMember();
        BeanUtils.copyProperties(umsMemberParam, umsMember);
        umsMember.setCreateTime(new Date());
        umsMember.setStatus(1);
        umsMember.setMemberLevelId(new Long(4));
        umsMember.setNickname(umsMember.getUsername());
        umsMember.setPhone(umsMember.getUsername());
        umsMember.setIcon("https://binggle-1253769387.cos.ap-guangzhou.myqcloud.com/test/images/2019/5/22/d14b9d5f-e2c6-41ad-8cac-f7f40440a26c.jpg");
        //查询是否有相同用户名的用户
        UmsMemberExample example = new UmsMemberExample();
        example.createCriteria().andUsernameEqualTo(umsMember.getUsername());
        List<UmsMember> umsMemberList = memberMapper.selectByExample(example);
        if (umsMemberList.size() > 0) {
            return null;
        }
        //将密码进行加密操作
        String encodePassword = passwordEncoder.encode(umsMember.getPassword());
        umsMember.setPassword(encodePassword);
        memberMapper.insert(umsMember);
        return umsMember;
    }

    @Override
    public int reset(UmsMemberParam umsMemberParam) {
        UmsMember umsMember = new UmsMember();
        BeanUtils.copyProperties(umsMemberParam, umsMember);
        //查询是否有相同用户名的用户
        UmsMemberExample example = new UmsMemberExample();
        example.createCriteria().andUsernameEqualTo(umsMember.getUsername());

        List<UmsMember> umsMemberList = memberMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(umsMemberList)) {
            return -1;
        }
        umsMember.setId(umsMemberList.get(0).getId());
        //将密码进行加密操作
        String encodePassword = passwordEncoder.encode(umsMember.getPassword());
        umsMember.setPassword(encodePassword);
        int i = memberMapper.updateByPrimaryKeySelective(umsMember);
        if(i > 0){
            return 0;
        }else{
            return -2;
        }
    }

    @Override
    public String login(String username, String password) {
        String token = null;
        //密码需要客户端加密后传递
        try {
            UmsMember memberDetails = getMemberByUsername(username);
            if(!passwordEncoder.matches(password,memberDetails.getPassword())){
                throw new BadCredentialsException("密码不正确");
            }
            token = jwtTokenUtil.generateToken1(memberDetails);
            insertLoginLog(username);
        } catch (AuthenticationException e) {
            LOGGER.warn("登录异常:{}", e.getMessage());
        }
        return token;
    }

    /**
     * 添加登录记录
     * @param username 用户名
     */
    private void insertLoginLog(String username) {
        UmsMember member = getMemberByUsername(username);
        UmsMemberLoginLog loginLog = new UmsMemberLoginLog();
        loginLog.setMemberId(member.getId());
        loginLog.setCreateTime(new Date());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        loginLog.setIp(request.getRemoteAddr());
        loginLogMapper.insert(loginLog);
    }

    @Override
    public String refreshToken(String oldToken) {
        String token = oldToken.substring(tokenHead.length());
        if (jwtTokenUtil.canRefresh(token)) {
            return jwtTokenUtil.refreshToken(token);
        }
        return null;
    }

    @Override
    public UmsMember getItem(Long id) {
        return memberMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<UmsMember> list(String name, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsMemberExample example = new UmsMemberExample();
        UmsMemberExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(name)) {
            criteria.andUsernameLike("%" + name + "%");
            example.or(example.createCriteria().andNicknameLike("%" + name + "%"));
        }
        return memberMapper.selectByExample(example);
    }

    @Override
    public int update(Long id, UmsMember member) {
        member.setId(id);
        //密码已经加密处理，需要单独修改
        member.setPassword(null);
        return memberMapper.updateByPrimaryKeySelective(member);
    }

    @Override
    public int delete(Long id) {
        return memberMapper.deleteByPrimaryKey(id);
    }
}
