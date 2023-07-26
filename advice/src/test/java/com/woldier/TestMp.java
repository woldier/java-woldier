package com.woldier;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.woldier.entity.Plaza;
import com.woldier.entity.vo.PlazaTreeVo;
import com.woldier.entity.vo.PlazaVo;
import com.woldier.mapper.PlazaMapper;
import com.woldier.mapper.UserMapper;
import com.woldier.service.PlazaService;
import com.woldier.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    @Test
    public void test4(){
        Page<PlazaVo> page = plazaService.getPageV2(1, 5);
        System.out.println(page);
    }


    @Test
    public void test5(){
        Page<PlazaTreeVo> page = plazaService.getPageV3(1, 5);
        System.out.println(page);
    }

    @Test
    public void test6(){
        Page<Plaza> page = new Page<>(1,5);
        LambdaQueryWrapper<Plaza> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(Plaza::getParentId); //条件查询父节点分页
        Page<Plaza> parentPage = plazaService.page(page, queryWrapper); //分页查询

        List<PlazaTreeVo> voList = parentPage.getRecords().stream().map(plaza -> {  //转为树状list
            PlazaTreeVo treeVo = new PlazaTreeVo();
            BeanUtils.copyProperties(plaza, treeVo);
            return treeVo;
        }).collect(Collectors.toList());

        Page<PlazaTreeVo> treeVoPage = new Page<>(); //转page类型
        BeanUtils.copyProperties(parentPage,treeVoPage,"records"); //对象拷贝
        treeVoPage.setRecords(voList); //设置数据
        if(voList.size()!=0){
            findKid(voList); //递归查询
        }
        System.out.println(voList);
    }

    /**
     * 递归调用
     * 为了减少网络id次数,做法是吧所有的父亲节点全部找出来,然后用in来查询
     * 把所有子节点查询出来再来链接到其父节点
     * @param roots
     */
    private void findKid(List<PlazaTreeVo> roots){
        List<Long> parentIds = roots.stream().map(PlazaTreeVo::getId).collect(Collectors.toList()); //拿到所有父亲的id
        if(parentIds.size()==0) return; //如果父亲的id为null 结束递归
        LambdaQueryWrapper<Plaza> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Plaza::getParentId,parentIds);  //条件查询
        List<Plaza> list = plazaService.list(queryWrapper); //查询得到所有的list
        //然后分配到对应的root
        HashMap<Long, List<PlazaTreeVo>> map = new HashMap<>();  //父节点map,用于连接子节点
        roots.forEach(  //便利所有父亲节点,然后把id存入map中,方便后面根据id去找
                vo->{
                    if(vo.getKids()==null) vo.setKids(new ArrayList<PlazaTreeVo>()); //创建
                    map.put(vo.getId(),vo.getKids());
                }
        );
        list.forEach(e->{  //遍历查询到的所有子节点,并且进行类型装欢然后拷贝到父亲节点
            PlazaTreeVo vo = new PlazaTreeVo();
            BeanUtils.copyProperties(e,vo);//对象拷贝
            map.get(e.getParentId()).add(vo); //设置孩子
        });
        List<PlazaTreeVo> kids = new ArrayList<>();  //便利得到所有子节点,去递归查询这些节点的孩子
        roots.forEach(e->{//遍历
            kids.addAll(e.getKids());
        });
        findKid(kids);  //递归调用
    }

}
