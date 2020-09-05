package com.atguigu.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 采购信息
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 19:07:49
 */
@Data
@TableName("wms_purchase")
public class PurchaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@TableId
	private Long id;
	/**
	 *
	 */
	private Long assigneeId;
	/**
	 *
	 */
	private String assigneeName;
	/**
	 *
	 */
	private String phone;
	/**
	 *
	 */
	private Integer priority;
	/**
	 *
	 */
	private Integer status;
	/**
	 *
	 */
	private Long wareId;
	/**
	 *
	 */
	private BigDecimal amount;
	/**
	 *
	 */
	private Date createTime;
	/**
	 *
	 */
	private Date updateTime;

	/**
	 * 采购单ID
	 */
	@TableField(exist = false)
	private Long purchaseId;

	/**
	 * 采需求ID列表
	 */
	@TableField(exist = false)
	private List<Long> items;

}
