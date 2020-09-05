package com.atguigu.gulimall.order.constant;

public class OrderConst {

    public static final String REDIS_ORDER_COMMIT_TOKEN = "order:commit_token:";

    public static final String SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    public static final int ORDER_AUTO_CONFIRM_DAY= 7;

    public static final String ORDER_TIMEOUT_EXPRESS= "30m";
}
