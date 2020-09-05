package com.atguigu.gulimall.product;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.zengtengpeng.operation.RedissonObject;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    private RedissonObject redissonObject;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void contextLoads() {
        redissonObject.setValue("hello", String.format("world_%s", UuidUtils.generateUuid()));
        String hello = redissonObject.getValue("hello");
        System.out.println(hello);
        redissonObject.delete("hello");
    }

    @Test
    public void clearAllTest() {
        RKeys rKeys = redissonClient.getKeys();
        rKeys.getKeysStream().forEach(System.out::println);
        //rKeys.getKeysStream().forEach(e -> System.out.println(redissonClient.getBucket(e).get()));
        rKeys.deleteByPattern("*");
    }

    @Test
    public void redissonTest1() {
        RBucket<String> hello = redissonClient.getBucket("hello");
        hello.set(UuidUtils.generateUuid());
        RBucket<String> bucket = redissonClient.getBucket("hello");
        String s = bucket.getAndDelete();
        System.out.println(s);
    }

}
