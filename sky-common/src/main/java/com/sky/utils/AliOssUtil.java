package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建ObjectMetadata，设置文件为公共读权限
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);

            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes), metadata);
        } catch (OSSException oe) {
            log.error("Caught an OSSException, Error Message: {}, Error Code: {}, Request ID: {}",
                    oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId());
            throw new RuntimeException("文件上传到OSS失败: " + oe.getErrorMessage(), oe);
        } catch (ClientException ce) {
            log.error("Caught an ClientException, Error Message: {}", ce.getMessage());
            throw new RuntimeException("OSS客户端异常: " + ce.getMessage(), ce);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
