package com.wolder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wolder.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
