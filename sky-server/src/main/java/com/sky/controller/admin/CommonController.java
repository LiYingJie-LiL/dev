package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonController {

    private final AliOssProperties aliOssProperties;
    private final AliOssUtil aliOssUtil;

    public CommonController(AliOssProperties aliOssProperties, AliOssUtil aliOssUtil) {
        this.aliOssProperties = aliOssProperties;
        this.aliOssUtil = aliOssUtil;
    }

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);

        try{
            //原始文件名
            String originalFilename=file.getOriginalFilename();
            //截取原始文件名的后缀  dfdfdf.png
            String extensin=originalFilename.substring(originalFilename.lastIndexOf("."));
            //构造新文件的名称
            String objectName= UUID.randomUUID().toString()+extensin;

            //文件的请求路径
            String filePath=aliOssUtil.upload(file.getBytes(),objectName);
            return Result.success(filePath);
        }catch (IOException e){
            log.error("文件上传IO异常：", e);
        }catch (RuntimeException e){
            log.error("文件上传OSS异常：", e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);

    }
}
