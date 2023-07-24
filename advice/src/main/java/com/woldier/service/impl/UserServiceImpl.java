package com.woldier.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.woldier.entity.User;
import com.woldier.mapper.UserMapper;
import com.woldier.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
