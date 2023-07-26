package com.woldier.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.woldier.entity.Plaza;
import com.woldier.entity.vo.PlazaTreeVo;
import com.woldier.entity.vo.PlazaVo;

public interface PlazaService extends IService<Plaza> {
    /*
    * 分页查询
    * */
    Page<PlazaVo> getPage(int current,int size);
    Page<PlazaVo> getPageV2(int current, int size);
    Page<PlazaTreeVo> getPageV3(int current, int size);
}
