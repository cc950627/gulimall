package com.atguigu.common.utils;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author 程城
 * @date 2021/1/15 11:42
 */
public class OptionalCollection {

    private static final OptionalCollection EMPTY = new OptionalCollection();

    private final Collection<Object> value;

    private OptionalCollection() {
        this.value = null;
    }

    public static OptionalCollection empty() {
        @SuppressWarnings("unchecked")
        OptionalCollection t = EMPTY;
        return t;
    }

    private OptionalCollection(Collection<Object> value) {
        this.value = Objects.requireNonNull(value);
    }

    public static OptionalCollection of(Collection<Object> value) {
        return new OptionalCollection(value);
    }

    public static OptionalCollection ofNullable(Collection<Object> value) {
        return CollectionUtils.isEmpty(value) ? empty() : of(value);
    }

    public Collection<Object> get() {
        if (CollectionUtils.isEmpty(value)) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return CollectionUtils.isNotEmpty(value);
    }

    public void ifPresent(Consumer<? super Collection<Object>> consumer) {
        if (CollectionUtils.isNotEmpty(value)) {
            consumer.accept(value);
        }
    }

    public OptionalCollection filter(Predicate<? super Collection<Object>> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }

    public OptionalCollection map(Function<? super Collection<Object>, ? extends Collection<Object>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();
        }
        else {
            return OptionalCollection.ofNullable(mapper.apply(value));
        }
    }

    public OptionalCollection flatMap(Function<? super Collection<Object>, OptionalCollection> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();
        } else {
            return Objects.requireNonNull(mapper.apply(value));
        }
    }
}
