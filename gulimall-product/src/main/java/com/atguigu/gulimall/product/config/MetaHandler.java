package com.atguigu.gulimall.product.config;

import com.atguigu.common.to.UserInfoTO;
import com.atguigu.gulimall.product.interceptor.LoginUserInterceptor;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 处理新增和更新的基础数据填充，配合POBase和MyBatisPlusConfig使用
 *
 * @author wbo
 * @version 1.0
 * @date 2020/04/14
 */
@Slf4j
@Component
public class MetaHandler implements MetaObjectHandler {
    /**
     * 新增数据执行
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        UserInfoTO userInfoTO = Optional.ofNullable(LoginUserInterceptor.loginUser.get()).orElseGet(UserInfoTO::new);
        this.strictInsertFill(metaObject, "createBy", userInfoTO::getId, Long.class);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "modifyBy", userInfoTO::getId, Long.class);
        this.strictInsertFill(metaObject, "modifyTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "version", () -> 1, Integer.class);
        this.strictInsertFill(metaObject, "deleted", () -> 0, Integer.class);
    }

    /**
     * 更新数据执行
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        UserInfoTO userInfoTO = Optional.ofNullable(LoginUserInterceptor.loginUser.get()).orElseGet(UserInfoTO::new);
        this.strictUpdateFill(metaObject, "modifyBy", userInfoTO::getId, Long.class);
        this.strictUpdateFill(metaObject, "modifyTime", LocalDateTime::now, LocalDateTime.class);
    }
}
