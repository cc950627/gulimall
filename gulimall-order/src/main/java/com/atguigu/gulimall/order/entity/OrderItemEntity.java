package com.atguigu.gulimall.order.entity;

import com.atguigu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 订单项信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 18:53:59
 */
@Data
@TableName("oms_order_item")
public class OrderItemEntity extends BaseEntity {
	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * order_id
	 */
	private Long orderId;
	/**
	 * order_sn
	 */
	private String orderSn;
	/**
	 * spu_id
	 */
	private Long spuId;
	/**
	 * spu_name
	 */
	private String spuName;
	/**
	 * spu_pic
	 */
	private String spuPic;
	/**
	 * 品牌
	 */
	private String spuBrand;
	/**
	 * 商品分类id
	 */
	private Long categoryId;
	/**
	 * 商品sku编号
	 */
	private Long skuId;
	/**
	 * 商品sku名字
	 */
	private String skuName;
	/**
	 * 商品sku图片
	 */
	private String skuPic;
	/**
	 * 商品sku价格
	 */
	private BigDecimal skuPrice;
	/**
	 * 商品购买的数量
	 */
	private Integer skuQuantity;
	/**
	 * 商品销售属性组合（JSON）
	 */
	private String skuAttrsVals;
	/**
	 * 商品促销分解金额
	 */
	private BigDecimal promotionAmount;
	/**
	 * 优惠券优惠分解金额
	 */
	private BigDecimal couponAmount;
	/**
	 * 积分优惠分解金额
	 */
	private BigDecimal integrationAmount;
	/**
	 * 该商品经过优惠后的分解金额
	 */
	private BigDecimal realAmount;
	/**
	 * 赠送积分
	 */
	private Integer giftIntegration;
	/**
	 * 赠送成长值
	 */
	private Integer giftGrowth;

}
