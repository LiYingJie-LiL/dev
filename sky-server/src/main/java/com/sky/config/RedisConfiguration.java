package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 编写Redis配置类
 */
@Configuration
@Slf4j
public class RedisConfiguration {

    //把方法返回的 `redisTemplate` 对象交给 **Spring IOC 容器管理**。

    //方法参数：RedisConnectionFactory redisConnectionFactory ——>SpringBoot 自动根据 `application.yml` 里的
    // redis 地址、端口、密码配置，自动生成这个连接工厂
    //连接工厂 = 负责创建和维持 Redis 网络连接
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象...");

        //手动实例化模板对象
        RedisTemplate redisTemplate=new RedisTemplate();

        //设置redis的连接工厂对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //设置redis key的序列化器
        /*什么是序列化？
        *  因为Redis 只存储字节数组 (byte [])，Java 代码里你传入的 key 是字符串对象，必须转成字节才能发给 Redis；
        * 读取时字节转回 Java 对象，这个转换过程就是序列化

          * StringRedisSerializer()
          * 使用字符串序列化：直接把字符串转为普通 utf-8 字节。
存入的 key 就是纯净字符串，redis 命令行直接正常查看，解决 key 乱码问题。
         */
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        //把配置完成的模板对象交给 Spring，之后全局注入使用
        return redisTemplate;

    }
}
