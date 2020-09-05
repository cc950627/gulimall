/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.atguigu.common.utils;

/**
 * 常量
 *
 * @author Mark sunlightcs@gmail.com
 */
public class Constant {
	/** 超级管理员ID */
	public static final int SUPER_ADMIN = 1;
    /**
     * 当前页码
     */
    public static final String PAGE = "page";
    /**
     * 每页显示记录数
     */
    public static final String LIMIT = "limit";
    /**
     * 排序字段
     */
    public static final String ORDER_FIELD = "sidx";
    /**
     * 排序方式
     */
    public static final String ORDER = "order";
    /**
     *  升序
     */
    public static final String ASC = "asc";

    /**
     *  产品分类信息的缓存Key
     */
    public static final String REDIS_CATEGORY_KEY = "redis_gulimall_product_category";

    /**
     *  redis存储验证码的前缀
     */
    public static final String REDIS_SMS_CODE_PREFIX = "redis_gulimall_sms_code:";

    /**
     *  redis存储验证码倒计时的前缀
     */
    public static final String REDIS_SMS_CODE_TIMEOUT_PREFIX = "redis_gulimall_sms_code_time:";

    /**
     * cookie跨域配置
     */
    public static final String REDIS_DOMAIN_NAME = "gulimall.com";

    /**
     * cookie名字
     */
    public static final String REDIS_COOKIE_NAME = "loginUser";

    /**
     * 秒杀活动前缀
     */
    public static final String REDIS_SESSION_CACHE_PREFIX = "seckill:session:";

    /**
     * 商品上架前缀
     */
    public static final String REDIS_SKUKILL_CACHE_PREFIX = "seckill:skus:";

    /**
	 * 菜单类型
	 *
	 * @author chenshun
	 * @email sunlightcs@gmail.com
	 * @date 2016年11月15日 下午1:24:29
	 */
    public enum MenuType {
        /**
         * 目录
         */
    	CATALOG(0),
        /**
         * 菜单
         */
        MENU(1),
        /**
         * 按钮
         */
        BUTTON(2);

        private int value;

        MenuType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 定时任务状态
     *
     * @author chenshun
     * @email sunlightcs@gmail.com
     * @date 2016年12月3日 上午12:07:22
     */
    public enum ScheduleStatus {
        /**
         * 正常
         */
    	NORMAL(0),
        /**
         * 暂停
         */
    	PAUSE(1);

        private int value;

        ScheduleStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 云服务商
     */
    public enum CloudService {
        /**
         * 七牛云
         */
        QINIU(1),
        /**
         * 阿里云
         */
        ALIYUN(2),
        /**
         * 腾讯云
         */
        QCLOUD(3);

        private int value;

        CloudService(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * 分页配置
     */
    public enum PageDefaut {
        /**
         * 搜索列表页
         */
        SEARCH_LIST(1, 100);

        private int currPage;

        private int pageSize;

        PageDefaut(int currPage, int pageSize) {
            this.currPage = currPage;
            this.pageSize = pageSize;
        }

        public int getCurrPage() {
            return currPage;
        }

        public int getPageSize() {
            return pageSize;
        }
    }

}
