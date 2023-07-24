package com.woldier.entity.vo;

import com.woldier.entity.Plaza;
import lombok.Data;

@Data
public class PlazaVo extends Plaza {
    private String nikeName; //拓展的user字段
    private String img; //拓展的user字段
}
