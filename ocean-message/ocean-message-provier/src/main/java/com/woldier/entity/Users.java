package com.woldier.entity;

import lombok.Data;
import lombok.ToString;

/**
 * description 数据库实体类
 *
 * @author: woldier wong
 * @date: 2023/8/27$ 15:49$
 */
@Data
@ToString
public class Users {
    private Long id;
    private String name;
    private String phone;
}
