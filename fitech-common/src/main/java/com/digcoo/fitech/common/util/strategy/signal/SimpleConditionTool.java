package com.digcoo.fitech.common.util.strategy.signal;

import com.digcoo.fitech.common.enums.SignalType;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.util.indicator.MacdIndicator;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class SimpleConditionTool {
    /**
     *
     * 判断远离黄金位
     * 1、黄金block两端
     * 2、黄金block区间（黄金block不交叉）
     *
     */
    public static boolean checkDeviationGoldBlock(Candlestick c0, List<MacdIndicator.MacdPoint> macdPoints) {

        Pair<MacdIndicator.MacdPoint, MacdIndicator.MacdPoint> latestGreenGoldMACDPointPair = MacdIndicator.getLatestGreenGoldMACDPointPair(macdPoints);
        Pair<MacdIndicator.MacdPoint, MacdIndicator.MacdPoint> latestRedGoldMACDPointPair = MacdIndicator.getLatestRedGoldMACDPointPair(macdPoints);

        Pair<BigDecimal, BigDecimal> greenGoldPair = null;
        if (latestGreenGoldMACDPointPair != null) {
            BigDecimal greenGoldHigh = null;
            BigDecimal greenGoldLow = null;
            MacdIndicator.MacdPoint left = latestGreenGoldMACDPointPair.getLeft();
            if (left != null) {
                greenGoldHigh = left.getTicker().getHigh();
                greenGoldLow = left.getTicker().getLow();
            }

            MacdIndicator.MacdPoint right = latestGreenGoldMACDPointPair.getRight();
            if (right != null) {
                greenGoldHigh = greenGoldHigh.compareTo(right.getTicker().getHigh()) > 0 ? greenGoldHigh : right.getTicker().getHigh();
                greenGoldLow = greenGoldLow.compareTo(right.getTicker().getLow()) < 0 ? greenGoldLow : right.getTicker().getLow();
            }

            greenGoldPair = Pair.of(greenGoldLow, greenGoldHigh);
        }

        Pair<BigDecimal, BigDecimal> redGoldPair = null;
        if (latestRedGoldMACDPointPair != null) {
            BigDecimal redGoldHigh = BigDecimal.ZERO;
            BigDecimal redGoldLow = BigDecimal.ZERO;
            MacdIndicator.MacdPoint left = latestRedGoldMACDPointPair.getLeft();
            if (left != null) {
                redGoldHigh = left.getTicker().getHigh();
                redGoldLow = left.getTicker().getLow();
            }

            MacdIndicator.MacdPoint right = latestRedGoldMACDPointPair.getRight();
            if (right != null) {
                redGoldHigh = redGoldHigh.compareTo(right.getTicker().getHigh()) > 0 ? redGoldHigh : right.getTicker().getHigh();
                redGoldLow = redGoldLow.compareTo(right.getTicker().getLow()) < 0 ? redGoldLow : right.getTicker().getLow();
            }

            redGoldPair = Pair.of(redGoldLow, redGoldHigh);
        }


        BigDecimal minGoldPrice = greenGoldPair.getLeft().compareTo(redGoldPair.getLeft()) < 0 ? greenGoldPair.getLeft() : redGoldPair.getLeft();
        BigDecimal maxGoldPrice = greenGoldPair.getRight().compareTo(redGoldPair.getRight()) > 0 ? greenGoldPair.getRight() : redGoldPair.getRight();

        //c0的价格在黄金block两端之外
        if (c0.getClose().compareTo(minGoldPrice) < 0 || c0.getClose().compareTo(maxGoldPrice) > 0) {
            return true;
        }

        //c0的价格在黄金block区间
        Pair<BigDecimal, BigDecimal> maxGoldBlock = null;
        if (greenGoldPair.getLeft().compareTo(redGoldPair.getRight()) >= 0) {
            maxGoldBlock = greenGoldPair;
        }
        if (redGoldPair.getLeft().compareTo(greenGoldPair.getRight()) >= 0) {
            maxGoldBlock = redGoldPair;
        }

        Pair<BigDecimal, BigDecimal> minGoldBlock = null;
        if (greenGoldPair.getRight().compareTo(redGoldPair.getLeft()) <= 0) {
            minGoldBlock = greenGoldPair;
        }
        if (redGoldPair.getRight().compareTo(greenGoldPair.getLeft()) <= 0) {
            minGoldBlock = redGoldPair;
        }

        if (maxGoldBlock != null && minGoldBlock != null) {

            if (c0.getClose().compareTo(minGoldBlock.getRight()) >= 0
                && c0.getClose().compareTo(maxGoldBlock.getLeft()) <= 0) {
                return true;
            }

        }

        return false;
    }

}
