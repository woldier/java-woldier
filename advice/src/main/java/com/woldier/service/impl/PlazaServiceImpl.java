package com.woldier.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.woldier.entity.Plaza;
import com.woldier.entity.User;
import com.woldier.entity.vo.PlazaTreeVo;
import com.woldier.entity.vo.PlazaVo;
import com.woldier.mapper.PlazaMapper;
import com.woldier.service.PlazaService;
import com.woldier.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlazaServiceImpl extends ServiceImplAdvice<PlazaMapper, Plaza> implements PlazaService {
    @Autowired
    private UserService userService;

    /**
     * 分页查询
     * @param current 页数
     * @param size 条数
     * @return
     */
//    @Override
//    public Page<PlazaVo> getPage(int current, int size) {
//        Page<Plaza> page = new Page<>(current,size);
//        page(page); //查询到page
//        //回表查询
//        List<PlazaVo> collect = page.getRecords().stream().map(e -> { //对象映射
//            PlazaVo plazaVo = new PlazaVo();
//            BeanUtils.copyProperties(e, plazaVo);//对象拷贝
//            return plazaVo;
//        }).collect(Collectors.toList());
//        collect.forEach(e->{ //查询
//            User byId = userService.getById(e.getUserId());
//            e.setImg(byId.getImg());
//            e.setNikeName(byId.getNikeName());
//        });
//        Page<PlazaVo> res = new Page<>();
//        BeanUtils.copyProperties(page,res);
//        res.setRecords(collect);
//        return res;
//    }


    /**
     * 分页查询
     * @param current 页数
     * @param size 条数
     * @return
     */
    @Override
    public Page<PlazaVo> getPage(int current, int size) {
        List<Plaza> list= lambdaQuery().list();

        //TODO 不同服务调用?跨库调用?
        return pageHelper(current,size, PlazaVo.class,
                e->{
            User user = userService.getById(e.getUserId());
            e.setNikeName(user.getNikeName());
            e.setImg(user.getImg());
        });



    }
    @Override
    public Page<PlazaVo> getPageV2(int current, int size) {
        List<Plaza> list= lambdaQuery().list();
        //TODO 不同服务调用?跨库调用?
        return pageHelperV2(current,size, PlazaVo.class,
                Plaza::getUserId,//告诉是对那个字段做id转name
                userService::listByIds, // 查询语句
                User::getId //告诉额外信息表的主键
              );
    }

    @Override
    public Page<PlazaTreeVo> getPageV3(int current, int size) {
        List<Plaza> list= lambdaQuery().list();
        //TODO 不同服务调用?跨库调用?
        return pageHelperV2(current,size, PlazaTreeVo.class,
                Plaza::getUserId,//告诉是对那个字段做id转name
                userService::listByIds, // 查询语句
                User::getId //告诉额外信息表的主键
        );
    }


}
