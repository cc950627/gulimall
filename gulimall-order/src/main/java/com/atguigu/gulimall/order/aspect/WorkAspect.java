package com.atguigu.gulimall.order.aspect;

import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author 程城
 * @date 2021/2/8 14:22
 */
@Aspect
@Component
public class WorkAspect {

    private final static Logger logger = LoggerFactory.getLogger(WorkAspect.class);

    @Before(value = "execution(* com.atguigu.gulimall.order.service.impl.*.*(..))")
    public void before(JoinPoint joinPoint) throws TransactionException {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        logger.info("拦截到需要分布式事务的方法," + method.getName());
        // 此处可用redis或者定时任务来获取一个key判断是否需要关闭分布式事务
        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();
        tx.begin(300000, "test-client");
        logger.info("创建分布式事务完毕" + tx.getXid());
    }

    @AfterThrowing(throwing = "e", pointcut = "execution(* com.atguigu.gulimall.order.service.impl.*.*(..))")
    public void doRecoveryActions(Throwable e) throws TransactionException {
        logger.info("方法执行异常:{}", e.getMessage());
        if (StringUtils.isNotBlank(RootContext.getXID())) {
            GlobalTransactionContext.reload(RootContext.getXID()).rollback();
        }
    }
}
