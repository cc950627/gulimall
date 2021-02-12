package com.atguigu.gulimall.product.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.atguigu.gulimall.product.dao")
public class MybatisConfing {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置请求的页面大于最大页后操作， true回到第一页，false 继续请求  默认false
        paginationInnerInterceptor.setOverflow(false);
        // 设置最大单页限制数量，默认无限制
        paginationInnerInterceptor.setMaxLimit(1000L);
        // 数据库类型(根据类型获取应使用的分页方言)
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInnerInterceptor.setOptimizeJoin(true);

        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 自动分页
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        // 乐观锁
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // sql性能规范
        // interceptor.addInnerInterceptor(new IllegalSQLInnerInterceptor());
        // 防止全表更新与删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * 避免缓存出现问题
     * @return
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.setUseDeprecatedExecutor(false);
    }

    /**
     * 自动填充功能,主要是自动给四个审计字段赋值
     *
     * @return
     */
    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new MetaHandler());
        return globalConfig;
    }
}
