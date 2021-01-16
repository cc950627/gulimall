package com.atguigu.common.utils;


import org.apache.commons.lang3.StringUtils;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author 程城
 * @date 2020/12/6 17:35
 */
public final class OptionalString {


    private static final OptionalString EMPTY = new OptionalString();


    private final String value;


    private OptionalString() {
        this.value = null;
    }


    public static OptionalString empty() {
        @SuppressWarnings("unchecked")
        OptionalString t = EMPTY;
        return t;
    }


    private OptionalString(String value) {
        this.value = Objects.requireNonNull(value);
    }


    public static OptionalString of(String value) {
        return new OptionalString(value);
    }


    public static OptionalString ofNullable(String value) {
        return StringUtils.isBlank(value) ? empty() : of(value);
    }


    public String get() {
        if (StringUtils.isBlank(value)) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }


    public boolean isPresent() {
        return StringUtils.isNotBlank(value);
    }


    public void ifPresent(Consumer<? super String> consumer) {
        if (StringUtils.isNotBlank(value)) {
            consumer.accept(value);
        }
    }


    public OptionalString filter(Predicate<? super String> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }


    public OptionalString map(Function<? super String, ? extends String> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();
        }
        else {
            return OptionalString.ofNullable(mapper.apply(value));
        }
    }


    public<U> OptionalString flatMap(Function<? super String, OptionalString> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();
        } else {
            return Objects.requireNonNull(mapper.apply(value));
        }
    }


    public String orElse(String other) {
        return StringUtils.isNotBlank(value) ? value : other;
    }


    public String orElseGet(Supplier<? extends String> other) {
        return StringUtils.isNotBlank(value) ? value : other.get();
    }


    public <X extends Throwable> String orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (StringUtils.isNotBlank(value)) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OptionalString)) {
            return false;
        }

        OptionalString other = (OptionalString) obj;
        return Objects.equals(value, other.value);
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return StringUtils.isNotBlank(value)
                ? String.format("OptionalString[%s]", value)
                : "OptionalString.empty";
    }
}
