package com.sky.service;

import com.sky.dto.SetmealDTO;
import org.springframework.stereotype.Service;

/**
 * 套餐相关
 */

public interface SetMealService {

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

}
