package com.atguigu.gulimall.product.entity;

import com.atguigu.common.base.BaseEntity;
import com.atguigu.common.valid.ListValue;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

import static com.atguigu.common.valid.ValidatedGroup.Add;
import static com.atguigu.common.valid.ValidatedGroup.Update;

/**
 * 品牌
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 16:39:44
 */
@Data
@TableName("pms_brand")
public class BrandEntity extends BaseEntity {
	/**
	 * 品牌id
	 */
	@TableId
	@NotNull(message = "新增不能指定ID", groups = Update.class)
	@Null(message = "修改必须指定ID", groups = Add.class)
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空", groups = Add.class)
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotBlank(message = "品牌logo不能为空", groups = Add.class)
	@URL(message = "品牌logo地址不合法", groups = {Add.class, Update.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(message = "显示状态不能为空", groups = Add.class)
	@ListValue(values = {0, 1}, message = "显示状态必须是0或1", groups = {Add.class, Update.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotBlank(message = "检索首字母不能为空", groups = Add.class)
	@Pattern(regexp = "^[a-zA=Z]$", message = "检索首字母必须是a-z或A-Z", groups = {Add.class, Update.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(message = "排序字段不能为空", groups = Add.class)
	@Min(value = 0, message = "排序字段必须大于等于0", groups = {Add.class, Update.class})
	private Integer sort;

}
