package com.digcoo.fitech.common.util;

import java.math.BigDecimal;

public class CompareUtils {
    public static boolean biggerThan(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) > 0;
    }

    public static boolean biggerAndEqualThan(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) >= 0;
    }


    public static boolean smallerThan(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) > 0;
    }

    public static boolean smallerAndEqualThan(BigDecimal a, BigDecimal b) {
        return a.compareTo(b) >= 0;
    }
}
