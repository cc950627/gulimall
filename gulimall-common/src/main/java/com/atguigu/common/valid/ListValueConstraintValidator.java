package com.atguigu.common.valid;

import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> set = Sets.newHashSet();

    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] values = constraintAnnotation.values();
        Stream.of(values).flatMapToInt(Arrays::stream).forEach(set::add);
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (CollectionUtils.isEmpty(set)) {
            return true;
        }
        return Stream.of(value).anyMatch(set::contains);
    }
}
