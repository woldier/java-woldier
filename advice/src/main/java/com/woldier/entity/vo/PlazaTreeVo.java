package com.woldier.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class PlazaTreeVo extends PlazaVo {
    private List<PlazaTreeVo> kids;
}
