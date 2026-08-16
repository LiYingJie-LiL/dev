package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 套餐业务实现
 */
@Service

public class SetmealServiceImpl implements SetmealService {

        @Autowired
        private SetmealDishMapper setmealDishMapper;
        @Autowired
        private SetmealMapper setmealMapper;
        @Autowired
        private DishMapper dishMapper;
        /**
         * 新增套餐，同时需要保存套餐和菜品的关联关系
         * @param setmealDTO
         */
        @Override
        @Transactional
        public void saveWithDish(SetmealDTO setmealDTO) {
            Setmeal setmeal=new Setmeal();
            BeanUtils.copyProperties(setmealDTO,setmeal);

            //向套餐表插入套餐
            setmealMapper.insert(setmeal);
            //获取生成的套餐id
            Long setmealId=setmeal.getId();

            // 1.从前端传过来的SetmealDTO中，拿到套餐包含的菜品集合 List<SetmealDish>
            List<SetmealDish> setmealDishes= setmealDTO.getSetmealDishes();


            // 2.遍历每一道套餐菜品，给每一个菜品对象设置套餐id
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });


            //保存套餐和菜品的关联关系
            setmealDishMapper.insertBatch(setmealDishes);


        }

        @Override
        public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
            //开启分页查询
            PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
            Page< SetmealVO> page=setmealMapper.pageQuery(setmealPageQueryDTO);
            return new PageResult(page.getTotal(),page.getResult());

        }

        /**
         * 删除套餐
         * @param ids
         */
        @Override
        @Transactional
        public void delete(List<Long> ids) {
            //判断当前套餐是否能够删除 ---是否存在起售的套餐？？
            for(Long id:ids){
                Setmeal setmeal=setmealMapper.getById(id);
                if(setmeal.getStatus()== StatusConstant.ENABLE){
                    //当前套餐处于起售中，不能删除。需要通过异常抛出
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
                }
            }

            //删除套餐表中的套餐数据
            for(Long id:ids){
                setmealMapper.deleteById(id);
                //删除套餐关联的菜品数据(删除套餐菜品关系表中的数据)
                setmealDishMapper.deleteBySetmealId(id);
            }

        }

        /**
         * 根据id查询套餐和关联的菜品数据
         * @param id
         * @return
         */
        @Override
        public SetmealVO getByIdWithDish(Long id) {
            //根据套餐id查询套餐数据
            Setmeal setmeal=setmealMapper.getById(id);
            if (setmeal == null) {
                throw new RuntimeException("套餐不存在");
            }
            //根据套餐id查询相关联的菜品数据
            List<SetmealDish> setmealDishes=setmealDishMapper.getBySetmealId(id);
            //将查询到的数据封装到VO
            SetmealVO setmealVO=new SetmealVO();
            BeanUtils.copyProperties(setmeal,setmealVO);
            setmealVO.setSetmealDishes(setmealDishes);
            return setmealVO;

        }

        /**
         * 修改套餐
         * @param setmealDTO
         */
        @Override
        @Transactional
        public void updateWithDish(SetmealDTO setmealDTO) {
            Setmeal setmeal=new Setmeal();
            BeanUtils.copyProperties(setmealDTO,setmeal);

            //修改套餐表基本信息
            setmealMapper.update(setmeal);

            //2.删除当前套餐原本绑定的所有菜品关联
            Long setmealId = setmeal.getId();
            setmealDishMapper.deleteBySetmealId(setmealId);

            //3.取出DTO里新的菜品列表，填充套餐id，批量新增关联
            List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
            if(setmealDishList != null && setmealDishList.size() > 0){
                for (SetmealDish setmealDish : setmealDishList) {
                    //重点：给每一条关联数据绑定套餐id
                    setmealDish.setSetmealId(setmealId);
                }
                //批量插入（推荐写批量方法，不要循环单次insert）
                setmealDishMapper.insertBatch(setmealDishList);
            }

        }

        /**
         * 条件查询
         * @param setmeal
         * @return
         */
        public List<Setmeal> list(Setmeal setmeal) {
            List<Setmeal> list = setmealMapper.list(setmeal);
            return list;
        }

        /**
         * 根据id查询菜品选项
         * @param id
         * @return
         */
        public List<DishItemVO> getDishItemById(Long id) {
            return setmealMapper.getDishItemBySetmealId(id);
        }

        /**
         * 套餐起售、停售
         * @param status
         * @param id
         */
        public void startOrStop(Integer status, Long id) {
            //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
            if(status == StatusConstant.ENABLE){
                //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
                List<Dish> dishList = dishMapper.getBySetmealId(id);
                if(dishList != null && dishList.size() > 0){
                    dishList.forEach(dish -> {
                        if(StatusConstant.DISABLE == dish.getStatus()){
                            throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                        }
                    });
                }
            }

            Setmeal setmeal = Setmeal.builder()
                    .id(id)
                    .status(status)
                    .build();
            setmealMapper.updateStatus(setmeal);
        }



    }
