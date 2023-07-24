package com.woldier;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.woldier.entity.Plaza;
import com.woldier.entity.vo.PlazaVo;
import com.woldier.mapper.PlazaMapper;
import com.woldier.mapper.UserMapper;
import com.woldier.service.PlazaService;
import com.woldier.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestMp {
    @Autowired
    UserMapper userMapper;
    @Autowired
    PlazaMapper plazaMapper;

    @Autowired
    UserService userService;
    @Autowired
    PlazaService plazaService;

    @Test
    public void test() {
//        userMapper.selectById(1);
        Page<Plaza> plazaPage = new Page<>(1, 5);
        Page<Plaza> page = plazaService.page(plazaPage);
        System.out.println(page);

    }

    /**
    *
    * description 基于mp重写page的查询实现多表查询,这样做需要自定义一个vo,以及查询的sql,不过sql的格式基本是一致的
    *
    * @author: woldier
    * @date: 2023/7/24 下午4:47
    */
    @Test
    public void test2() {
        Page<PlazaVo> plazaPage = new Page<>(1, 5);
        QueryWrapper<PlazaVo> qw = new QueryWrapper<>();
        //TODO qw可以加入客制化查询条件
        qw.ge("u.id",10);
        qw.ge("p.id",50);
        List<PlazaVo> page = plazaMapper.getPlazaPage(plazaPage, qw);
        System.out.println(page);
    }


    @Test
    public void test3(){
        Page<PlazaVo> page = plazaService.getPage(1, 5);
        System.out.println(page);
    }


}
