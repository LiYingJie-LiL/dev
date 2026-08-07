package com.sky.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T>响应数据泛型，代表返回data字段的数据类型
 */
@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; // 返回的数据主体，查询、新增等业务数据放这里


    /**
     * 响应成功（无返回数据）
     * @return 统一结果对象，code=1，无data、msg
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 1;
        return result;
    }


    /**
     * 响应成功（携带返回数据）
     * @param object 需要返回给前端的数据
     * @return 统一结果对象，code=1，携带业务数据data
     */
    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    /**
     * 响应失败
     * @param msg 失败提示信息
     * @return 统一结果对象，code=0，携带错误提示msg
     */
    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}
