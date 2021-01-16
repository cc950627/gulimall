package com.atguigu.gulimall.xxljob.jobhandler;

import com.atguigu.gulimall.xxljob.config.XxlJobConfig;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author chengcheng
 */
@Component
public class HelloWordHandler {
    private final Logger logger = LoggerFactory.getLogger(HelloWordHandler.class);

    @XxlJob(value = "helloWordHandler", init = "init", destroy = "destroy")
    public ReturnT<String> execute(String param) {
        XxlJobLogger.log("=== HelloWordHandler start===");
        XxlJobLogger.log("hello world.");
        System.out.println(param);
        XxlJobLogger.log("=== HelloWordHandler end ===");
        return ReturnT.SUCCESS;
    }

    public void init(){
        logger.info("init");
    }
    public void destroy(){
        logger.info("destory");
    }
}
