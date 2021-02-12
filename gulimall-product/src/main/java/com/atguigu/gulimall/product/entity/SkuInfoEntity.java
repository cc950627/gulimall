package com.atguigu.gulimall.product.entity;

import com.atguigu.common.base.BaseEntity;
import com.atguigu.common.to.MemberPriceTO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * sku信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 16:39:45
 */
@Data
@TableName("pms_sku_info")
public class SkuInfoEntity extends BaseEntity {
	/**
	 * skuId
	 */
	@TableId
	private Long skuId;
	/**
	 * spuId
	 */
	private Long spuId;
	/**
	 * sku名称
	 */
	private String skuName;
	/**
	 * sku介绍描述
	 */
	private String skuDesc;
	/**
	 * 所属分类id
	 */
	private Long catalogId;
	/**
	 * 品牌id
	 */
	private Long brandId;
	/**
	 * 默认图片
	 */
	private String skuDefaultImg;
	/**
	 * 标题
	 */
	private String skuTitle;
	/**
	 * 副标题
	 */
	private String skuSubtitle;
	/**
	 * 价格
	 */
	private BigDecimal price;
	/**
	 * 销量
	 */
	private Long saleCount;

	@TableField(exist = false)
	private List<SkuImagesEntity> images;

	@TableField(exist = false)
	private List<SkuSaleAttrValueEntity> attr;

	@TableField(exist = false)
	private int fullCount;

	@TableField(exist = false)
	private BigDecimal discount;

	@TableField(exist = false)
	private int countStatus;

	@TableField(exist = false)
	private BigDecimal fullPrice;

	@TableField(exist = false)
	private BigDecimal reducePrice;

	@TableField(exist = false)
	private int priceStatus;

	@TableField(exist = false)
	private List<MemberPriceTO> memberPrice;

}
