package com.woldier.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.woldier.entity.Plaza;
import com.woldier.entity.vo.PlazaVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlazaMapper extends BaseMapper<Plaza> {
    List<PlazaVo> getPlazaPage(Page<PlazaVo> page,@Param(Constants.WRAPPER) Wrapper<PlazaVo> wrapper);
}
