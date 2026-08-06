package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {

//    插入数据
    @Insert("insert into category (type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "values " +
            "(#{type},#{name},#{sort},#{status},#{createTime},#{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

//    分类分页查询
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

//    根据id删除分类
    @Delete("delete from category where id=#{id}")
    void deleteById(Long id);


//    根据id修改分类
    void update(Category category);

//    根据类型查询分类
    List<Category> list(Integer type);
}
