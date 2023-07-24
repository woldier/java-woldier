package com.woldier.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.woldier.entity.Plaza;
import com.woldier.entity.vo.PlazaVo;

public interface PlazaService extends IService<Plaza> {
    /*
    * 分页查询
    * */
    Page<PlazaVo> getPage(int current,int size);
}
