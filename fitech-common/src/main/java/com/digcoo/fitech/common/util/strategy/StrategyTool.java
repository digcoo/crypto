package com.digcoo.fitech.common.util.strategy;

import com.digcoo.fitech.common.enums.TrendPeriodType;
import com.digcoo.fitech.common.enums.SignalType;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.util.indicator.MacdIndicator;
import com.digcoo.fitech.common.util.strategy.signal.ComSignalTool;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public final class StrategyTool {


    public static TrendPeriodType checkMacdTrendPeriod(Candlestick c0, List<MacdIndicator.MacdPoint> macdPoints) {


        //先绿后红-----红低绿高（顺周期趋势）
        if (MacdIndicator.checkLatestRedGold(macdPoints)) {
            MacdIndicator.MacdPoint latestRedGoldMACDPoint = MacdIndicator.getLatestRedGoldMACDPoint(macdPoints);
            MacdIndicator.MacdPoint latestGreenGoldMACDPoint = MacdIndicator.getLatestGreenGoldMACDPoint(macdPoints);
            //-----红低绿高(区间1)
            if(MacdIndicator.checkGreenGoldGreatThanRedGold(macdPoints)
                    && c0.getClose().compareTo(latestRedGoldMACDPoint.getTicker().getLow()) >= 0
                    && c0.getClose().compareTo(latestRedGoldMACDPoint.getTicker().getHigh()) <= 0) {
                return TrendPeriodType.SPRING;
            }

            //-----红低绿高(区间2)
            if(MacdIndicator.checkGreenGoldGreatThanRedGold(macdPoints)
                    && c0.getClose().compareTo(latestRedGoldMACDPoint.getTicker().getHigh()) > 0
                    && c0.getClose().compareTo(latestGreenGoldMACDPoint.getTicker().getHigh()) <= 0) {
                return TrendPeriodType.SUMMER;
            }

            //-----红低绿高(区间3)
            if(MacdIndicator.checkGreenGoldGreatThanRedGold(macdPoints)
                    && c0.getClose().compareTo(latestGreenGoldMACDPoint.getTicker().getHigh()) > 0) {
                return TrendPeriodType.SUMMER;
            }

            //-----绿低红高(区间3)（顶部背离：加速或危险信号）
            if(MacdIndicator.checkRedGoldGreatThanGreenGold(macdPoints)
                    && c0.getClose().compareTo(latestRedGoldMACDPoint.getTicker().getLow()) >= 0) {
                return TrendPeriodType.SUMMER;
            }
        }

        //先红后绿-----红低绿高（逆周期趋势）
        if (MacdIndicator.checkLatestGreenGold(macdPoints)) {
            MacdIndicator.MacdPoint latestRedGoldMACDPoint = MacdIndicator.getLatestRedGoldMACDPoint(macdPoints);
            MacdIndicator.MacdPoint latestGreenGoldMACDPoint = MacdIndicator.getLatestGreenGoldMACDPoint(macdPoints);

            //-----红低绿高(区间3)
            if(MacdIndicator.checkGreenGoldGreatThanRedGold(macdPoints)
                    && c0.getClose().compareTo(latestGreenGoldMACDPoint.getTicker().getLow()) >= 0) {
                return TrendPeriodType.WINTER_STRUGGLE;
            }

            //-----红低绿高(区间2)
            if(MacdIndicator.checkGreenGoldGreatThanRedGold(macdPoints)
                    && c0.getClose().compareTo(latestGreenGoldMACDPoint.getTicker().getLow()) < 0
                    && c0.getClose().compareTo(latestRedGoldMACDPoint.getTicker().getHigh()) >= 0) {
                return TrendPeriodType.WINTER;
            }

            //-----红低绿高(区间1)
            if(MacdIndicator.checkGreenGoldGreatThanRedGold(macdPoints)
                    && c0.getClose().compareTo(latestRedGoldMACDPoint.getTicker().getHigh()) < 0
                    && c0.getClose().compareTo(latestRedGoldMACDPoint.getTicker().getLow()) >= 0) {
                return TrendPeriodType.SPRING_STRUGGLE;
            }

            //-----绿低红高(区间3)（底部背离：建仓信号）
            if(MacdIndicator.checkRedGoldGreatThanGreenGold(macdPoints)
                    && c0.getClose().compareTo(latestGreenGoldMACDPoint.getTicker().getLow()) >= 0) {
                return TrendPeriodType.SPRING_STRUGGLE;
            }
        }

        return TrendPeriodType.WINTER;
    }



    /**
     * 判断黄金位的回踩信号
     * @param candlesticks
     * @param macdPoints
     * @return
     */
    public static SignalType checkFallMacdGoldSignal(List<Candlestick> candlesticks, List<MacdIndicator.MacdPoint> macdPoints) {

        Pair<MacdIndicator.MacdPoint, MacdIndicator.MacdPoint> latestRedGoldMACDPointPair = MacdIndicator.getLatestRedGoldMACDPointPair(macdPoints);
        Pair<MacdIndicator.MacdPoint, MacdIndicator.MacdPoint> latestGreenGoldMACDPointPair = MacdIndicator.getLatestGreenGoldMACDPointPair(macdPoints);

        SignalType signalType = SignalType.NO_SIGNAL;
        //红金叉区间--- 回踩
        signalType = ComSignalTool.checkFallGoldMacdSignal(candlesticks, latestRedGoldMACDPointPair);
        if (signalType != SignalType.NO_SIGNAL) {
            return signalType;
        }

        //绿金叉区间--- 回踩
        signalType = ComSignalTool.checkFallGoldMacdSignal(candlesticks, latestGreenGoldMACDPointPair);
        if (signalType != SignalType.NO_SIGNAL) {
            return signalType;
        }

        return SignalType.NO_SIGNAL;
    }


    /**
     * 判断黄金位的突破信号
     * @param candlesticks
     * @param macdPoints
     * @return
     */
    public static SignalType checkCrossMacdGoldSignal(List<Candlestick> candlesticks, List<MacdIndicator.MacdPoint> macdPoints) {

        Pair<MacdIndicator.MacdPoint, MacdIndicator.MacdPoint> latestRedGoldMACDPointPair = MacdIndicator.getLatestRedGoldMACDPointPair(macdPoints);
        Pair<MacdIndicator.MacdPoint, MacdIndicator.MacdPoint> latestGreenGoldMACDPointPair = MacdIndicator.getLatestGreenGoldMACDPointPair(macdPoints);

        SignalType signalType = SignalType.NO_SIGNAL;
        //红金叉区间--- 突破
        signalType = ComSignalTool.checkCrossMacdGoldSignal(candlesticks, latestRedGoldMACDPointPair);
        if (signalType != SignalType.NO_SIGNAL) {
            return signalType;
        }

        //绿金叉区间--- 突破
        signalType = ComSignalTool.checkCrossMacdGoldSignal(candlesticks, latestGreenGoldMACDPointPair);
        if (signalType != SignalType.NO_SIGNAL) {
            return signalType;
        }

        return SignalType.NO_SIGNAL;
    }

}
