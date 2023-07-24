package com.woldier;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.woldier.entity.Plaza;
import com.woldier.entity.vo.PlazaVo;
import com.woldier.mapper.PlazaMapper;
import com.woldier.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestMp {
    @Autowired
    private PlazaMapper plazaMapper;
    @Autowired
    private UserMapper userMapper;
    @Test
    public void test(){
//        userMapper.selectById(1);
        Page<Plaza> plazaVoPage = new Page<>(1, 5);
    }

}
