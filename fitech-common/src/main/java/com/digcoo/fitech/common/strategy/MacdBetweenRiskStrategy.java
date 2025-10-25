//package com.digcoo.fitech.common.strategy;
//
//import com.digcoo.fitech.common.config.Config;
//import com.digcoo.fitech.common.enums.CandlestickPeriod;
//import com.digcoo.fitech.common.enums.OrderSide;
//import com.digcoo.fitech.common.enums.SignalType;
//import com.digcoo.fitech.common.enums.StrategyType;
//import com.digcoo.fitech.common.model.Candlestick;
//import com.digcoo.fitech.common.model.Position;
//import com.digcoo.fitech.common.model.Signal;
//import com.digcoo.fitech.common.strategy.base.TradeStrategy;
//import com.digcoo.fitech.common.util.signal.SimpleSignalTool;
//import com.digcoo.fitech.common.util.indicator.MacdIndicator;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.util.Arrays;
//import java.util.List;
//
///**
// * MACD区间：凹突破
// */
//@Component
//public class MacdBetweenRiskStrategy implements TradeStrategy {
//
//    public MacdBetweenRiskStrategy() {
//    }
//
//    @Override
//    public StrategyType getStrategyType() {
//        return StrategyType.MACD_OVER_RISE;
//    }
//
//    @Override
//    public Signal generateSignal(String symbol, CandlestickPeriod period, List<Candlestick> candlesticks, Position currentPosition) {
//
//        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
//
//        List<MacdIndicator.MacdPoint> macdPoints = MacdIndicator.calculateMacd(candlesticks);
//
//        SignalType signalType = SimpleSignalTool.checkRiseBetweenMacdSignal(candlesticks, macdPoints);
//        if (SignalType.NO_SIGNAL == signalType) {
//            return Signal.NO_SIGNAL;
//        }
//
//        long triggerTime = System.currentTimeMillis();
//        if (SignalType.BUY == signalType && checkBuyTrend(candlesticks, macdPoints)) {
//            BigDecimal delegatePrice = calDelegatePrice(candlesticks, SignalType.BUY);
//            BigDecimal delegateQty = calDelegateQty(SignalType.BUY, delegatePrice);
//            BigDecimal tpPrice = calTpPrice(candlesticks, SignalType.BUY);
//            BigDecimal slPrice = calSlPrice(candlesticks, SignalType.BUY);
//            return Signal.builder()
//                    .symbol(symbol)
//                    .period(period)
//                    .strategyType(getStrategyType())
//                    .orderSide(OrderSide.BUY)
//                    .triggerTimestamp(triggerTime)
//                    .delegatePrice(delegatePrice)
//                    .delegateQty(delegateQty)
//                    .tpPrice(tpPrice)
//                    .slPrice(slPrice)
//                    .currentPrice(candlestick0.getClose())
//                    .build();
//        } else if (SignalType.SELL == signalType && checkSellTrend(candlesticks, macdPoints)) {
//            BigDecimal delegatePrice = calDelegatePrice(candlesticks, SignalType.SELL);
//            BigDecimal delegateQty = calDelegateQty(SignalType.SELL, delegatePrice);
//            BigDecimal tpPrice = calTpPrice(candlesticks, SignalType.SELL);
//            BigDecimal slPrice = calSlPrice(candlesticks, SignalType.SELL);
//            return Signal.builder()
//                    .symbol(symbol)
//                    .period(period)
//                    .strategyType(getStrategyType())
//                    .orderSide(OrderSide.SELL)
//                    .triggerTimestamp(triggerTime)
//                    .delegatePrice(delegatePrice)
//                    .delegateQty(delegateQty)
//                    .tpPrice(tpPrice)
//                    .slPrice(slPrice)
//                    .currentPrice(candlestick0.getClose())
//                    .build();
//        }
//
//        return Signal.NO_SIGNAL;
//    }
//
//
//    @Override
//    public BigDecimal calDelegatePrice(List<Candlestick> candlesticks, SignalType signalType) {
//        return BigDecimal.ZERO;
//    }
//
//    @Override
//    public BigDecimal calDelegateQty(SignalType signalType, BigDecimal delegatePrice) {
//        return BigDecimal.ZERO;
//    }
//
//    @Override
//    public BigDecimal calTpPrice(List<Candlestick> candlesticks, SignalType signalType) {
//        return BigDecimal.ZERO;
//    }
//
//    @Override
//    public BigDecimal calSlPrice(List<Candlestick> candlesticks, SignalType signalType) {
//        return BigDecimal.ZERO;
//    }
//
//}