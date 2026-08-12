package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {

    //提出一个常量
    private static final String key="SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺相关的营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺相关的营业状态为：{}",status==1?"营业中":"打样中");
        redisTemplate.opsForValue().set(key,status);
        return Result.success();
    }

    /**
     * 获取店铺的营业状态
     */
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status=(Integer) redisTemplate.opsForValue().get(key);
        log.info("获取店铺相关的营业状态为：{}",status==1?"营业中":"打样中");
        return Result.success(status);
    }

}
