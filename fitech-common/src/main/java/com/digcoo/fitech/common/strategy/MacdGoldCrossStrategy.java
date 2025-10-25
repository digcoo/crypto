package com.digcoo.fitech.common.strategy;

import com.digcoo.fitech.common.enums.*;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.model.Position;
import com.digcoo.fitech.common.model.Signal;
import com.digcoo.fitech.common.strategy.base.TradeStrategy;
import com.digcoo.fitech.common.util.indicator.MacdIndicator;
import com.digcoo.fitech.common.util.strategy.StrategyTool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * MACD趋势策略（回踩）
 * 1、阳春阶段
 *
 *
 * 2、周期
 */
@Component
public class MacdGoldCrossStrategy implements TradeStrategy {

    public MacdGoldCrossStrategy() {
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.MACD_GOLD_BACK_CROSS;
    }

    @Override
    public Signal generateSignal(String symbol, CandlestickPeriod period, List<Candlestick> candlesticks, Position currentPosition) {

        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        Candlestick candlestick1 = candlesticks.get(candlesticks.size() - 2);

        List<MacdIndicator.MacdPoint> macdPoints = MacdIndicator.calculateMacd(candlesticks);

        // 分析趋势周期
        TrendPeriodType trendPeriodType = StrategyTool.checkMacdTrendPeriod(candlestick0, macdPoints);

        //分析当前信号
        SignalType signalType = StrategyTool.checkCrossMacdGoldSignal(candlesticks, macdPoints);

        if (SignalType.NO_SIGNAL == signalType) {
            return Signal.NO_SIGNAL;
        }

        long triggerTime = System.currentTimeMillis();
        if (SignalType.BUY == signalType
                && trendPeriodType.isExpansion()
//                && checkBuyTrend(candlesticks, macdPoints)
        ) {
            BigDecimal delegatePrice = calDelegatePrice(candlesticks, SignalType.BUY);
            BigDecimal delegateQty = calDelegateQty(SignalType.BUY, delegatePrice);
            BigDecimal tpPrice = calTpPrice(candlesticks, SignalType.BUY);
            BigDecimal slPrice = calSlPrice(candlesticks, SignalType.BUY);
            return Signal.builder()
                    .symbol(symbol)
                    .period(period)
                    .strategyType(getStrategyType())
                    .orderSide(OrderSide.BUY)
                    .triggerTimestamp(triggerTime)
                    .delegatePrice(delegatePrice)
                    .delegateQty(delegateQty)
                    .tpPrice(tpPrice)
                    .slPrice(slPrice)
                    .currentPrice(candlestick0.getClose())
                    .build();
        } else if (SignalType.SELL == signalType
                && trendPeriodType.isExpansion()
//                && checkSellTrend(candlesticks, macdPoints)
        ) {
            BigDecimal delegatePrice = calDelegatePrice(candlesticks, SignalType.SELL);
            BigDecimal delegateQty = calDelegateQty(SignalType.SELL, delegatePrice);
            BigDecimal tpPrice = calTpPrice(candlesticks, SignalType.SELL);
            BigDecimal slPrice = calSlPrice(candlesticks, SignalType.SELL);
            return Signal.builder()
                    .symbol(symbol)
                    .period(period)
                    .strategyType(getStrategyType())
                    .orderSide(OrderSide.SELL)
                    .triggerTimestamp(triggerTime)
                    .delegatePrice(delegatePrice)
                    .delegateQty(delegateQty)
                    .tpPrice(tpPrice)
                    .slPrice(slPrice)
                    .currentPrice(candlestick0.getClose())
                    .build();
        }

        return Signal.NO_SIGNAL;
    }


    @Override
    public BigDecimal calDelegatePrice(List<Candlestick> candlesticks, SignalType signalType) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calDelegateQty(SignalType signalType, BigDecimal delegatePrice) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calTpPrice(List<Candlestick> candlesticks, SignalType signalType) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calSlPrice(List<Candlestick> candlesticks, SignalType signalType) {
        return BigDecimal.ZERO;
    }

}