package com.atguigu.gulimall.product.entity;

import com.atguigu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 品牌分类关联
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 16:39:44
 */
@Data
@TableName("pms_category_brand_relation")
public class CategoryBrandRelationEntity extends BaseEntity {
	/**
	 *
	 */
	@TableId
	private Long id;
	/**
	 * 品牌id
	 */
	private Long brandId;
	/**
	 * 分类id
	 */
	private Long catelogId;
	/**
	 *
	 */
	private String brandName;
	/**
	 *
	 */
	private String catelogName;

}
