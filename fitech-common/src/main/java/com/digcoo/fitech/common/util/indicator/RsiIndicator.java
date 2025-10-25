package com.digcoo.fitech.common.util.indicator;

import com.digcoo.fitech.common.model.Candlestick;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class RsiIndicator {

    public static List<RsiPoint> calculateRsi(List<Candlestick> tickers) {
        List<BigDecimal> closes = tickers.stream().map(Candlestick::getClose).collect(Collectors.toList());
        return calculateRsi(tickers, closes, 6);
    }

    /**
     * 计算RSI指标
     * @param prices 价格列表(按时间顺序排列，最新的价格在最后)
     * @param period 计算周期(通常为14)
     * @return RSI值列表(与输入价格列表长度相同，前period-1个为null)
     */
    private static List<RsiPoint> calculateRsi(List<Candlestick> candlesticks, List<BigDecimal> prices, int period) {
        if (prices == null || prices.size() < period) {
//            throw new IllegalArgumentException("价格数据不足或周期设置过大");
            return Collections.emptyList();
        }

        List<RsiPoint> rsiPoints = new ArrayList<>(candlesticks.size());

        List<Double> rsiValues = new ArrayList<>();
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();

        // 初始化前period-1个RSI值为null
        for (int i = 0; i < period - 1; i++) {
            rsiPoints.add(RsiPoint.builder()
                            .ticker(candlesticks.get(i))
                    .build());
        }

        // 计算价格变化
        for (int i = 1; i < prices.size(); i++) {
            double change = prices.get(i).doubleValue() - prices.get(i - 1).doubleValue();
            gains.add(change > 0 ? change : 0);
            losses.add(change < 0 ? -change : 0); // 下跌幅度取正值
        }

        // 计算初始平均上涨和下跌
        double avgGain = 0;
        double avgLoss = 0;

        for (int i = 0; i < period - 1; i++) {
            avgGain += gains.get(i);
            avgLoss += losses.get(i);
        }

        avgGain /= period;
        avgLoss /= period;

        // 计算第一个RSI值
        double rs = (avgLoss == 0) ? Double.POSITIVE_INFINITY : avgGain / avgLoss;
        double rsi = 100 - (100 / (1 + rs));
        rsiValues.add(rsi);

        rsiPoints.add(RsiPoint.builder()
                .ticker(candlesticks.get(period - 1))
                .build());

        // 计算后续RSI值(使用平滑移动平均)
        for (int i = period; i < prices.size() - 1; i++) {
            avgGain = (avgGain * (period - 1) + gains.get(i)) / period;
            avgLoss = (avgLoss * (period - 1) + losses.get(i)) / period;

            rs = (avgLoss == 0) ? Double.POSITIVE_INFINITY : avgGain / avgLoss;
            rsi = 100 - (100 / (1 + rs));
            rsiValues.add(rsi);

            rsiPoints.add(RsiPoint.builder()
                    .ticker(candlesticks.get(i))
                    .rsi1(new BigDecimal(rsi))
                    .build());
        }

        return rsiPoints;
    }


    @Data
    @Builder
    public static class RsiPoint {
        private Candlestick ticker;
        private BigDecimal rsi1;
        private BigDecimal rsi2;
        private BigDecimal rsi3;

        private String getOpenTimeStr() {
            return this.ticker.getOpenTimeStr();
        }

    }
}
