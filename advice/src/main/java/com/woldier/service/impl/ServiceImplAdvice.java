package com.woldier.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.woldier.entity.Plaza;
import com.woldier.entity.User;
import com.woldier.entity.vo.PlazaTreeVo;
import com.woldier.entity.vo.PlazaVo;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServiceImplAdvice<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {
    /**
     * description TODO
     *
     * @param current  当前页
     * @param size     数据条数
     * @param consumer 对于vo类要做的额外查找工作
     * @param cla      泛型参数
     * @return
     * @author: woldier
     * @date: 2023/7/24 下午5:26
     */
    @SuppressWarnings("all")
    protected <E> Page<E> pageHelper(int current, int size, Class<E> cla, Consumer<E> consumer) {
        Page<T> page = new Page<>(current, size);
        page(page); //查询到page

        //回表查询
        List<E> collect = page.getRecords().stream().map(e -> { //对象映射
            E vo = null;
            try {
                vo = (E) cla.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
            BeanUtils.copyProperties(e, vo);//对象拷贝
            return vo;
        }).collect(Collectors.toList());
        //查询
        //根据业务字段id,in中查询到所有的信息

        collect.forEach(consumer);


        Page<E> res = new Page<>();
        BeanUtils.copyProperties(page, res, "records");
        res.setRecords(collect);
        return res;
    }

    @SuppressWarnings("all")
    protected <E, EX> Page<E> pageHelperV2(
            int current,
            int size,
            Class<E> cla,
            SFunction<T, ? extends Serializable> getIdFromMain, //从主表中获取附表id
            Function<Collection<? extends Serializable>, List<EX>> listByIdFunc, //查询的方法
            SFunction<EX, ? extends Serializable> getIdFromExtra //从主表中获取附表id

    ) {

        Page<T> page = new Page<>(current, size);
        page(page); //查询到page
        //查询
        //根据业务字段id,in中查询到所有的信息
        List<? extends Serializable> ids = page.getRecords().stream().map(getIdFromMain).collect(Collectors.toList());
        List<EX> list = listByIdFunc.apply(ids);
        //对象拷贝,排除getId对应字段
        Map<? extends Serializable, EX> map = list.stream().collect(Collectors.toMap(getIdFromExtra, e -> e));
        //回表查询
        List<E> collect = page.getRecords().stream().map(e -> { //对象映射
            E vo = null;
            try {
                vo = (E) cla.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }

            Serializable id = getIdFromMain.apply(e); //获取对象额外信息id
            EX ex = map.get(id);
            BeanUtils.copyProperties(ex, vo);  //拷贝额外信息

            BeanUtils.copyProperties(e, vo);//对象拷贝
            return vo;
        }).collect(Collectors.toList());


        Page<E> res = new Page<>();
        BeanUtils.copyProperties(page, res, "records");
        res.setRecords(collect);
        return res;
    }

    @SuppressWarnings("all")
    protected <E, EX> List<E> pageHelperV3(
            List<T> list,
            Class<E> cla,
            SFunction<T, ? extends Serializable> getIdFromMain, //从主表中获取附表id
            Function<Collection<? extends Serializable>, List<EX>> listByIdFunc, //查询的方法
            SFunction<EX, ? extends Serializable> getIdFromExtra //从主表中获取附表id

    ) {



        //查询
        //根据业务字段id,in中查询到所有的信息
        List<? extends Serializable> ids = list.stream().map(getIdFromMain).collect(Collectors.toList());
        List<EX> listExtra = listByIdFunc.apply(ids);
        //对象拷贝,排除getId对应字段
        Map<? extends Serializable, EX> map = listExtra.stream().collect(Collectors.toMap(getIdFromExtra, e->e));
        //回表查询
        List<E> collect = list.stream().map(e -> { //对象映射
            E vo = null;
            try {
                vo = (E) cla.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }

            Serializable id = getIdFromMain.apply(e); //获取对象额外信息id
            EX ex = map.get(id);
            BeanUtils.copyProperties(ex, vo);  //拷贝额外信息

            BeanUtils.copyProperties(e, vo);//对象拷贝
            return vo;
        }).collect(Collectors.toList());



        return collect;
    }
//
//    @SuppressWarnings("all")
//    protected <E, EX> Page<E> pageHelperV3(
//            int current,
//            int size,
//            Class<E> cla,
//            SFunction<T, ? extends Serializable> getIdFromMain, //从主表中获取附表id
//            Function<Collection<? extends Serializable>, List<EX>> listByIdFunc, //查询的方法
//            SFunction<EX, ? extends Serializable> getIdFromExtra, //从主表中获取附表id
//            SFunction<T, ? extends Serializable> getPatentId,  //获取父亲id
//            SFunction<E,? extends Serializable> getVoId, //获取vo类中的id
//            SFunction<E,List<E>> getVoKid,//获取vo类中的孩子
//            Consumer<E> setVoKid //设置vo类中的孩子
//            ) {
//        Page<T> page = new Page<>(1,5);
//        LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.isNull(getPatentId); //条件查询父节点分页
//        Page<T> parentPage = this.page(page, queryWrapper); //分页查询
//
//        List<E> voList = parentPage.getRecords().stream().map(plaza -> {  //转为树状list
//            E vo = null;
//            try {
//                vo = (E) cla.newInstance();
//            } catch (InstantiationException ex) {
//                throw new RuntimeException(ex);
//            } catch (IllegalAccessException ex) {
//                throw new RuntimeException(ex);
//            }
//            BeanUtils.copyProperties(plaza, vo);
//            return vo;
//        }).collect(Collectors.toList());
//
//        Page<E> treeVoPage = new Page<>(); //转page类型
//        BeanUtils.copyProperties(parentPage,treeVoPage,"records"); //对象拷贝
//        treeVoPage.setRecords(voList); //设置数据
//        if(voList.size()!=0){
//            findKid(voList,getIdFromMain,getVoId,getVoKid,setVoKid); //递归查询
//        }
//        return null;
//    }
//
//
//    /**
//     * 递归调用
//     * 为了减少网络id次数,做法是吧所有的父亲节点全部找出来,然后用in来查询
//     * 把所有子节点查询出来再来链接到其父节点
//     * @param roots
//     */
//    @SuppressWarnings("all")
//    private <E> void findKid(
//            List<E> roots,
//            SFunction<T, ? extends Serializable> getIdFromMain, //从主表中获取附表id
//            SFunction<T, ? extends Serializable> getPatentId,  //获取父亲id
//            SFunction<E,? extends Serializable> getVoId, //获取vo类中的id
//            SFunction<E,List<E>> getVoKid, //获取vo类中的孩子
//            Consumer<E> setVoKid //获取vo类中的孩子
//    ){
//        List<? extends Serializable> parentIds = roots.stream().map(getVoId).collect(Collectors.toList()); //拿到所有父亲的id
//        if(parentIds.size()==0) return; //如果父亲的id为null 结束递归
//        LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.in(getPatentId,parentIds);  //条件查询
//        List<T> list = this.list(queryWrapper); //查询得到所有的list
//        //然后分配到对应的root
//        HashMap<? extends Serializable, List<E>> map = new HashMap<>();  //父节点map,用于连接子节点
//        roots.forEach(  //便利所有父亲节点,然后把id存入map中,方便后面根据id去找
//                vo->{
//                    if(getVoKid.apply(vo)==null)  setVoKid.accept((E) new ArrayList<E>()); //创建
//                    map.put(getVoId,getVoKid.apply(vo));
//                }
//        );
//        list.forEach(e->{  //遍历查询到的所有子节点,并且进行类型装欢然后拷贝到父亲节点
//            E vo =(E) new Object();
//            BeanUtils.copyProperties(e,vo);//对象拷贝
//            map.get(getPatentId.apply(e)).add(vo); //设置孩子
//        });
//        List<PlazaTreeVo> kids = new ArrayList<>();  //便利得到所有子节点,去递归查询这些节点的孩子
//        roots.forEach(e->{//遍历
//            kids.addAll(getVoKid.apply(e));
//        });
//        findKid(kids);  //递归调用
//    }
}
