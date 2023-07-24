package com.woldier.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.woldier.entity.Plaza;
import com.woldier.entity.User;
import com.woldier.entity.vo.PlazaVo;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServiceImplAdvice <M extends BaseMapper<T>, T> extends ServiceImpl<M,T> {
    /**
    *
    * description TODO
    *
    * @param current  当前页
    * @param size  数据条数
    * @param consumer  对于vo类要做的额外查找工作
    * @param cla  泛型参数
    * @return
    * @author: woldier
    * @date: 2023/7/24 下午5:26
    */
    @SuppressWarnings("all")
    protected <E> Page<E> pageHelper(int current, int size,  Class<E> cla,Consumer<E> consumer) {
        Page<T> page = new Page<>(current,size);
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
        collect.forEach(consumer);

        Page<E> res = new Page<>();
        BeanUtils.copyProperties(page,res);
        res.setRecords(collect);
        return res;
    }
}
