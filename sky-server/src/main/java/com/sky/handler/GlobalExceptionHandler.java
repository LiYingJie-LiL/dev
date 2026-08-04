package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler//依靠 Spring 的 @ExceptionHandler 注解，用来捕获程序抛出的指定异常 SQLIntegrityConstraintViolationException
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
       //SQLIntegrityConstraintViolationException:SQL 完整性约束异常

        //Duplicate entry '张三' for key 'tbl_user.unique_username'——>插入重复数据

        String message=ex.getMessage();
        if(message.contains("Duplicate entry")){
            String[] split=message.split(" ");//分隔字符串
            String username=split[2];
            String msg=username+ MessageConstant.ALREADY_EXISTS;//已存在
            return Result.error(msg);
        }else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
