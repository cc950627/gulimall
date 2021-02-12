package com.atguigu.gulimall.product.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum AttrTypeEnum {

    /**
     * 销售属性
     */
    ATTR_TYPE_SALE(0, "sale"),

    /**
     * 基本属性
     */
    ATTR_TYPE_BASE(1, "base");

    @EnumValue
    private final Integer value;

    @JsonValue
    private final String type;

    AttrTypeEnum(Integer value, String type) {
        this.type = type;
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public static AttrTypeEnum getAttrTypeEnum(String title) {
        for (AttrTypeEnum attrTypeEnum : AttrTypeEnum.values()) {
            if (Objects.equals(attrTypeEnum.getType(), title)) {
                return attrTypeEnum;
            }
        }
        return null;
    }
}
