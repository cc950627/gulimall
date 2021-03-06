package com.atguigu.gulimall.product.entity;

import com.atguigu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 属性分组
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 16:39:44
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity extends BaseEntity {
    /**
     * 分组id
     */
    @TableId
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;
    /**
     * 所属分类名称
     */
    @TableField(exist = false)
    private String catelogName;
    /**
     * 分类ID及其父节点ID
     */
    @TableField(exist = false)
    private List<Long> catelogIds;
    /**
     * 属性列表
     */
    @TableField(exist = false)
    private List<AttrEntity> attrs;

}
