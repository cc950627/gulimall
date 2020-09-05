package com.atguigu.gulimall.thirdparyt;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@SpringBootTest
class GulimallThirdParytApplicationTests {

    @Autowired
    private OSSClient ossClient;

    @Test
    public void testUpload() throws Exception {
        FileInputStream inputStream = new FileInputStream("C:\\Users\\chengcheng\\Desktop\\cedf692ac7ad11e9b8cf00163e009f5514642white.jpg");
        ossClient.putObject("study-chengcheng", "test.jpg", inputStream);
        ossClient.shutdown();
    }

}
