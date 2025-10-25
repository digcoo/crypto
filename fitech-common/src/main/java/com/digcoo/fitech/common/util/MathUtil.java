package com.digcoo.fitech.common.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class MathUtil {

    public static BigDecimal max(BigDecimal ... values) {
        return Arrays.asList(values).stream().filter(x -> Objects.nonNull(x)).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    public static BigDecimal min(BigDecimal ... values) {
        return Arrays.asList(values).stream().filter(x -> Objects.nonNull(x)).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    public static double max(Double ... values) {
        return Arrays.asList(values).stream().filter(x -> Objects.nonNull(x)).max(Double::compareTo).orElse(0.0);
    }

    public static double min(Double ... values) {
        return Arrays.asList(values).stream().filter(x -> Objects.nonNull(x)).min(Double::compareTo).orElse(0.0);
    }

    public static Integer max(Integer ... values) {
        return Arrays.asList(values).stream().filter(x -> Objects.nonNull(x)).max(Integer::compareTo).orElse(0);
    }

    public static Integer min(Integer ... values) {
        return Arrays.asList(values).stream().filter(x -> Objects.nonNull(x)).min(Integer::compareTo).orElse(0);
    }

    public static BigDecimal max(List<BigDecimal> values) {
        return values.stream().filter(x -> Objects.nonNull(x)).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    public static BigDecimal min(List<BigDecimal> values) {
        return values.stream().filter(x -> Objects.nonNull(x)).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    public static boolean between(BigDecimal target, BigDecimal v1, BigDecimal v2) {
        BigDecimal min = MathUtil.min(v1, v2);
        BigDecimal max = MathUtil.max(v1, v2);
        return target.compareTo(min) >= 0 && target.compareTo(max) <= 0;
    }


}
