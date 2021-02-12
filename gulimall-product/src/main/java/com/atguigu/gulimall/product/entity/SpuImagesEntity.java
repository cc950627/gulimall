package com.atguigu.gulimall.product.entity;

import com.atguigu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * spu图片
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-06-14 19:09:41
 */
@Data
@TableName("pms_spu_images")
public class SpuImagesEntity extends BaseEntity {
	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * spu_id
	 */
	private Long spuId;
	/**
	 * 图片名
	 */
	private String imgName;
	/**
	 * 图片地址
	 */
	private String imgUrl;
	/**
	 * 顺序
	 */
	private Integer imgSort;
	/**
	 * 是否默认图
	 */
	private Integer defaultImg;

}
