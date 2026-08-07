package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DishPageQueryDTO implements Serializable {

    private int page;//页码

    private int pageSize;//每页记录数

    private String name;//菜品名称

    //分类id
    private Integer categoryId;

    //状态 0表示禁用 1表示启用
    private Integer status;

}
