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
//import com.digcoo.fitech.common.util.MathUtil;
//import com.digcoo.fitech.common.util.signal.SimpleSignalTool;
//import com.digcoo.fitech.common.util.indicator.MacdIndicator;
//import jakarta.annotation.Resource;
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.List;
//
//
///**
// * 反包策略
// */
//@Component
//public class FanBaoStrategy implements TradeStrategy {
//
//    private final Config config;
//
//    public FanBaoStrategy(Config config) {
//        this.config = config;
//    }
//
//    @Override
//    public StrategyType getStrategyType() {
//        return StrategyType.FAN_BAO;
//    }
//
//    @Override
//    public Signal generateSignal(String symbol, CandlestickPeriod period, List<Candlestick> candlesticks, Position currentPosition) {
//
//        if (candlesticks.size() < 2) {
//            return Signal.NO_SIGNAL;
//        }
//
//        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
//        Candlestick candlestick1 = candlesticks.get(candlesticks.size() - 2);
//
//        // 上一周期反包
//        SignalType signalType = SimpleSignalTool.checkFanBaoSignal(candlestick0, candlestick1);
//        if (SignalType.NO_SIGNAL == signalType) {
//            return Signal.NO_SIGNAL;
//        }
//
//        List<MacdIndicator.MacdPoint> macdPoints = MacdIndicator.calculateMacd(candlesticks);
//        long triggerTime = System.currentTimeMillis();
//        if (SignalType.BUY == signalType
////                && checkBuyTrend(candlesticks, macdPoints)
//        ) {
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
//        } else if (SignalType.SELL == signalType
////                && checkSellTrend(candlesticks, macdPoints)
//        ) {
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
//    public BigDecimal calDelegatePrice(List<Candlestick> candles, SignalType signalType) {
//        Candlestick candlestick1 = candles.get(candles.size() - 2);
//        Candlestick candlestick2 = candles.get(candles.size() - 3);
//
//        if (signalType == SignalType.BUY) {
//            BigDecimal priceRange = candlestick1.getClose().subtract(candlestick1.getOpen());
//            return candlestick1.getOpen().add(priceRange.multiply(new BigDecimal(0.5)));
//        } else {
//            BigDecimal priceRange = candlestick1.getClose().subtract(candlestick1.getOpen());
//            return candlestick1.getOpen().add(priceRange.multiply(new BigDecimal(0.5)));
//        }
//    }
//
//    @Override
//    public BigDecimal calDelegateQty(SignalType signalType, BigDecimal delegatePrice) {
//        // 根据风险管理和账户余额计算下单数量
////        BigDecimal riskAmount = portfolio.getCurrentBalance().multiply(config.getRiskPerTrade()).divide(new BigDecimal(100));
//        BigDecimal riskAmount = config.getInitialBalance();
//        return riskAmount.divide(delegatePrice, RoundingMode.HALF_UP);
//    }
//
//    public BigDecimal calTpPrice(List<Candlestick> candles, SignalType signalType) {
//        Candlestick candlestick1 = candles.get(candles.size() - 2);
//        Candlestick candlestick2 = candles.get(candles.size() - 3);
//
//        if (signalType == SignalType.BUY) {
//            BigDecimal priceRange = candlestick1.getClose().subtract(candlestick1.getLow());
//            BigDecimal profitPrice = candlestick1.getClose().add(priceRange.multiply(config.getFanbaoProfitPercent()));
//            BigDecimal maxProfitPrice = candlestick1.getClose().multiply(BigDecimal.ONE.add(config.getFanbaoMaxProfitPercent()));
//            return MathUtil.min(profitPrice, maxProfitPrice);
//        } else {
//            BigDecimal priceRange = candlestick1.getClose().subtract(candlestick1.getLow());
//            BigDecimal profitPrice = candlestick1.getClose().add(priceRange.multiply(config.getFanbaoProfitPercent()));
//            BigDecimal maxProfitPrice = candlestick1.getClose().multiply(BigDecimal.ONE.add(config.getFanbaoMaxProfitPercent()));
//            return MathUtil.max(profitPrice, maxProfitPrice);
//        }
//    }
//
//    @Override
//    public BigDecimal calSlPrice(List<Candlestick> candles, SignalType signalType) {
//        Candlestick candlestick1 = candles.get(candles.size() - 2);
//        Candlestick candlestick2 = candles.get(candles.size() - 3);
//
//        if (signalType == SignalType.BUY) {
//            return MathUtil.min(candlestick1.getLow(), candlestick2.getLow());
//        } else {
//            return MathUtil.max(candlestick1.getHigh(), candlestick2.getHigh());
//        }
//    }
//
//}
