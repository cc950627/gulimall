package com.atguigu.gulimall.search.vo;

import com.atguigu.common.utils.Constant;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Data
public class SearchParam implements Serializable {
    private static final long serialVersionUID = 1L;

    private String keyword;

    private Long catalogId;

    private String sort;

    private Boolean hasStock;

    private String skuPrice;

    private List<Long> brandIds;

    private List<String> attrs;

    private String _queryString;

    /**
     * 每页记录数
     */
    private int pageSize;

    /**
     * 当前页数
     */
    private int currPage;

    public void initPage(Constant.PageDefaut pageDefaut) {
		if (Objects.equals(this.currPage, 0)) {
			this.currPage = pageDefaut.getCurrPage();
		}
		if (Objects.equals(this.pageSize, 0)) {
			this.pageSize = pageDefaut.getPageSize();
		}
	}
}
