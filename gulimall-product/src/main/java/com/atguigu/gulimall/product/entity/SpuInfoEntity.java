package com.atguigu.gulimall.product.entity;

import com.atguigu.common.to.SpuBoundTO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * spu信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-06-14 19:09:41
 */
@Data
@TableName("pms_spu_info")
public class SpuInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 商品id
	 */
	@TableId
	private Long id;
	/**
	 * 商品名称
	 */
	private String spuName;
	/**
	 * 商品描述
	 */
	private String spuDescription;
	/**
	 * 所属分类id
	 */
	private Long catalogId;
	/**
	 * 品牌id
	 */
	private Long brandId;
	/**
	 *
	 */
	private BigDecimal weight;
	/**
	 * 上架状态[0 - 下架，1 - 上架]
	 */
	private Integer publishStatus;
	/**
	 *
	 */
	private Date createTime;
	/**
	 *
	 */
	private Date updateTime;

	@TableField(exist = false)
	private List<String> decript;

	@TableField(exist = false)
	private List<String> images;

	@TableField(exist = false)
	private List<ProductAttrValueEntity> baseAttrs;

	@TableField(exist = false)
	private List<SkuInfoEntity> skus;

	@TableField(exist = false)
	private SpuBoundTO bounds;

	@TableField(exist = false)
	private String catalogName;

	@TableField(exist = false)
	private String brandName;
}
