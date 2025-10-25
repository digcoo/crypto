package com.digcoo.fitech.common.strategy.base;

import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.model.Position;
import com.digcoo.fitech.common.model.Signal;
import com.digcoo.fitech.common.enums.SignalType;
import com.digcoo.fitech.common.enums.StrategyType;
import com.digcoo.fitech.common.util.MathUtil;
import com.digcoo.fitech.common.util.indicator.MacdIndicator;
import com.digcoo.fitech.common.util.indicator.RsiIndicator;

import java.math.BigDecimal;
import java.util.List;

public interface TradeStrategy {

    abstract StrategyType getStrategyType();
    abstract Signal generateSignal(String symbol, CandlestickPeriod period, List<Candlestick> candlesticks, Position currentPosition);
    abstract BigDecimal calDelegatePrice(List<Candlestick> candlesticks, SignalType signalType);
    abstract BigDecimal calDelegateQty(SignalType signalType, BigDecimal delegatePrice);
    abstract BigDecimal calTpPrice(List<Candlestick> candlesticks, SignalType signalType);

    abstract BigDecimal calSlPrice(List<Candlestick> candlesticks, SignalType signalType);

    default boolean checkBuyTrend(List<Candlestick> candlesticks, List<MacdIndicator.MacdPoint> macdPoints) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);

        MacdIndicator.MacdPoint latestRedGoldMACDPoint = MacdIndicator.getLatestRedGoldMACDPoint(macdPoints);
        MacdIndicator.MacdPoint latestGreenGoldMACDPoint = MacdIndicator.getLatestGreenGoldMACDPoint(macdPoints);
        MacdIndicator.MacdPoint latestGoldMACDPoint = MacdIndicator.getLatestGoldMACDPoint(macdPoints);


        if (latestRedGoldMACDPoint == null || latestGreenGoldMACDPoint == null) {
            return false;
        }

        List<RsiIndicator.RsiPoint> rsiPoints = RsiIndicator.calculateRsi(candlesticks);
        RsiIndicator.RsiPoint rsiPoint0 = rsiPoints.get(rsiPoints.size() - 1);
        
        if (latestGoldMACDPoint == latestGreenGoldMACDPoint) {
            if (candlestick0.getClose().compareTo(latestGoldMACDPoint.getTicker().getLow()) >= 0
                    && MathUtil.between(rsiPoint0.getRsi1(), new BigDecimal("0.3"), new BigDecimal("0.7"))
            ) {
                return true;
            }
        } else if (latestGoldMACDPoint == latestRedGoldMACDPoint) {
            if (candlestick0.getClose().compareTo(latestGoldMACDPoint.getTicker().getHigh()) >= 0
                    && MathUtil.between(rsiPoint0.getRsi1(), new BigDecimal("0.3"), new BigDecimal("0.7"))
            ) {
                return true;
            }
        }

        return false;
    }

    default boolean checkSellTrend(List<Candlestick> candlesticks, List<MacdIndicator.MacdPoint> macdPoints) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);

        MacdIndicator.MacdPoint latestRedGoldMACDPoint = MacdIndicator.getLatestRedGoldMACDPoint(macdPoints);
        MacdIndicator.MacdPoint latestGreenGoldMACDPoint = MacdIndicator.getLatestGreenGoldMACDPoint(macdPoints);
        MacdIndicator.MacdPoint latestGoldMACDPoint = MacdIndicator.getLatestGoldMACDPoint(macdPoints);

        List<RsiIndicator.RsiPoint> rsiPoints = RsiIndicator.calculateRsi(candlesticks);
        RsiIndicator.RsiPoint rsiPoint0 = rsiPoints.get(rsiPoints.size() - 1);

        if (latestRedGoldMACDPoint == null || latestGreenGoldMACDPoint == null) {
            return false;
        }

        if (latestGoldMACDPoint == latestGreenGoldMACDPoint) {
            if (candlestick0.getClose().compareTo(latestGoldMACDPoint.getTicker().getLow()) <= 0
                    && MathUtil.between(rsiPoint0.getRsi1(), new BigDecimal("0.4"), new BigDecimal("0.6"))) {
                return true;
            }
        } else if (latestGoldMACDPoint == latestRedGoldMACDPoint) {
            if (candlestick0.getClose().compareTo(latestGoldMACDPoint.getTicker().getHigh()) <= 0
                    && MathUtil.between(rsiPoint0.getRsi1(), new BigDecimal("0.4"), new BigDecimal("0.6"))) {
                return true;
            }
        }

        return false;

    }

}
