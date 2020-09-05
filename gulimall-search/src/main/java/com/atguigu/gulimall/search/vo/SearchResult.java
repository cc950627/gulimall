package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<SkuEsModel> products;

    private List<BrandVO> brands;

    private List<CatalogVO> catalogs;

    private List<AttrVO> attrs;

    private List<NavVO> navs;

    /**
     * 每页记录数
     */
    private int pageSize;

    /**
     * 当前页数
     */
    private int currPage;

    /**
     * 总记录数
     */
    private Long totalCount;

    /**
     * 总页数
     */
    private Long totalPage;

}
