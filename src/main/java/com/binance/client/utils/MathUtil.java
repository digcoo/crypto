package com.binance.client.utils;

import com.binance.client.model.enums.MAType;
import com.binance.client.model.market.MACross;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
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


    public static BigDecimal max(List<BigDecimal> values) {
        return values.stream().filter(x -> Objects.nonNull(x)).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    public static BigDecimal min(List<BigDecimal> values) {
        return values.stream().filter(x -> Objects.nonNull(x)).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    public static BigDecimal calCrossPointY(MACross.MALine ml1, MACross.MALine ml2) {
        Pair<BigDecimal, BigDecimal> A1 = Pair.of(new BigDecimal(1.0), ml1.getMALine().getLeft());
        Pair<BigDecimal, BigDecimal> A2 = Pair.of(new BigDecimal(2.0), ml1.getMALine().getRight());
        Pair<BigDecimal, BigDecimal> B1 = Pair.of(new BigDecimal(1.0), ml2.getMALine().getLeft());
        Pair<BigDecimal, BigDecimal> B2 = Pair.of(new BigDecimal(2.0), ml2.getMALine().getRight());

        boolean cross = false;
        if (ml2.getMALine().getLeft().compareTo(ml1.getMALine().getLeft()) <= 0
                && ml2.getMALine().getRight().compareTo(ml1.getMALine().getRight()) >= 0) {
            cross = true;
        }else if(ml1.getMALine().getLeft().compareTo(ml2.getMALine().getLeft()) <= 0
                && ml1.getMALine().getRight().compareTo(ml2.getMALine().getRight()) >= 0){
            cross = true;
        }
        if (!cross) {
            return null;
        }

        BigDecimal numerator = B2.getLeft().subtract(B1.getLeft()).multiply(A1.getRight().subtract(B1.getRight()))
                .subtract(B2.getRight().subtract(B1.getRight()).multiply(A1.getLeft().subtract(B1.getLeft())));
        BigDecimal denominator = B2.getRight().subtract(B1.getRight()).multiply(A2.getLeft().subtract(A1.getLeft()))
                .subtract(B2.getLeft().subtract(B1.getLeft()).multiply(A2.getRight().subtract(A1.getRight())));

        if (denominator.compareTo(BigDecimal.ZERO) == 0){
            return null;
        }

        BigDecimal ua = numerator.divide(denominator, 8, RoundingMode.HALF_DOWN);
        return A1.getRight().add(ua.multiply(A2.getRight().subtract(A1.getRight())));
    }

    public static void main(String [] args) {
//        MACross.MALine ml1 = new MACross.MALine(MAType.MA5, Pair.of(new BigDecimal(1.0163400000), new BigDecimal(1.0187400000)));
//
//        MACross.MALine ml2 = new MACross.MALine(MAType.MA10, Pair.of(new BigDecimal(1.0166900000), new BigDecimal(1.0142400000)));
//
//        System.out.println(calCrossPointY(ml1, ml2));
    }
}
