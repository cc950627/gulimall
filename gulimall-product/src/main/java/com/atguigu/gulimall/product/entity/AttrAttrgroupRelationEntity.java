package com.atguigu.gulimall.product.entity;

import com.atguigu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 属性&属性分组关联
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 16:39:44
 */
@Data
@TableName("pms_attr_attrgroup_relation")
public class AttrAttrgroupRelationEntity extends BaseEntity {
	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 属性id
	 */
	private Long attrId;
	/**
	 * 属性分组id
	 */
	private Long attrGroupId;
	/**
	 * 属性组内排序
	 */
	private Integer attrSort;

}
