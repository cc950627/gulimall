package com.atguigu.gulimall.product.entity;

import com.atguigu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * spu信息介绍
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-06-14 19:09:41
 */
@Data
@TableName("pms_spu_info_desc")
public class SpuInfoDescEntity extends BaseEntity {
	/**
	 * 商品id
	 */
	@TableId(type = IdType.INPUT)
	private Long spuId;
	/**
	 * 商品介绍
	 */
	private String decript;

}
