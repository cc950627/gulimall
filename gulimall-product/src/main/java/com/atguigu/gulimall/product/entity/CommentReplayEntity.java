package com.atguigu.gulimall.product.entity;

import com.atguigu.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品评价回复关系
 *
 * @author chengcheng
 * @email chengcheng634493683@qq.com
 * @date 2020-05-24 16:39:44
 */
@Data
@TableName("pms_comment_replay")
public class CommentReplayEntity extends BaseEntity {
	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 评论id
	 */
	private Long commentId;
	/**
	 * 回复id
	 */
	private Long replyId;

}
