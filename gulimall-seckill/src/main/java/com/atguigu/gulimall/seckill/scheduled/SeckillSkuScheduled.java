package com.atguigu.gulimall.seckill.scheduled;

import com.atguigu.gulimall.seckill.constant.SeckillConstant;
import com.atguigu.gulimall.seckill.service.SeckillService;
import com.zengtengpeng.annotation.Lock;
import com.zengtengpeng.enums.LockModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@EnableAsync
@EnableScheduling
@Service
public class SeckillSkuScheduled {

    @Autowired
    private SeckillService seckillService;

    @Lock(keys = SeckillConstant.UPLOAD_LOCK, lockModel = LockModel.REENTRANT, lockWatchdogTimeout = 10000, attemptTimeout = 10000)
    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkulatest3Days() {
        seckillService.uploadSeckillSkulatest3Days();
    }
}
