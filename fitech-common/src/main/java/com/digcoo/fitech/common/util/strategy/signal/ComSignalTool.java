package com.digcoo.fitech.common.util.strategy.signal;


import com.digcoo.fitech.common.enums.SignalType;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.util.indicator.MacdIndicator;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 复杂信号工具
 */
public final class ComSignalTool {

    /**
     * 回踩
     * @param candlesticks
     * @param goldMACDPointPair
     * @return
     */
    public static SignalType checkFallGoldMacdSignal(List<Candlestick> candlesticks, Pair<MacdIndicator.MacdPoint, MacdIndicator.MacdPoint> goldMACDPointPair) {
        if (goldMACDPointPair != null) {
            MacdIndicator.MacdPoint goldMACDPoint1 = goldMACDPointPair.getKey();
            MacdIndicator.MacdPoint goldMACDPoint2 = goldMACDPointPair.getValue();

            List<BigDecimal> goldPrices = new ArrayList<>();
            if (goldMACDPoint1 != null) {
                goldPrices.add(goldMACDPoint1.getTicker().getLow());
                goldPrices.add(goldMACDPoint1.getTicker().getHigh());
            }

            if (goldMACDPoint2 != null) {
                goldPrices.add(goldMACDPoint2.getTicker().getLow());
                goldPrices.add(goldMACDPoint2.getTicker().getHigh());
            }

            return SimpleSignalTool.checkFallGoldMacdSignal(candlesticks, goldPrices);

        }

        return SignalType.NO_SIGNAL;

    }


    /**
     * 突破
     * @param candlesticks
     * @param goldMACDPointPair
     * @return
     */
    public static SignalType checkCrossMacdGoldSignal(List<Candlestick> candlesticks, Pair<MacdIndicator.MacdPoint, MacdIndicator.MacdPoint> goldMACDPointPair) {
        if (goldMACDPointPair != null) {
            MacdIndicator.MacdPoint goldMACDPoint1 = goldMACDPointPair.getKey();
            MacdIndicator.MacdPoint goldMACDPoint2 = goldMACDPointPair.getValue();

            List<BigDecimal> goldPrices = new ArrayList<>();
            if (goldMACDPoint1 != null) {
                goldPrices.add(goldMACDPoint1.getTicker().getLow());
                goldPrices.add(goldMACDPoint1.getTicker().getHigh());
            }

            if (goldMACDPoint2 != null) {
                goldPrices.add(goldMACDPoint2.getTicker().getLow());
                goldPrices.add(goldMACDPoint2.getTicker().getHigh());
            }

            return SimpleSignalTool.checkCrossGoldMacdSignal(candlesticks, goldPrices);

        }

        return SignalType.NO_SIGNAL;

    }
}
