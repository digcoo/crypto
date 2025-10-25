package com.digcoo.fitech.common.util.strategy.signal;

import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.enums.SignalType;
import com.digcoo.fitech.common.model.Signal;
import com.digcoo.fitech.common.util.MathUtil;
import com.digcoo.fitech.common.util.indicator.MacdIndicator;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

public final class SimpleSignalTool {

    /**
     * 判断是否是反包信号
     *
     * @param c0
     * @param c1
     * @return
     */
    public static SignalType checkFanBaoSignal(Candlestick c0, Candlestick c1) {
        if (c0.getClose().compareTo(c1.getHigh()) > 0
                && c1.getClose().compareTo(c1.getOpen()) < 0) {
            return SignalType.BUY;
        } else if (c0.getClose().compareTo(c1.getLow()) < 0
                && c1.getClose().compareTo(c1.getOpen()) > 0) {
            return SignalType.SELL;
        }
        return SignalType.NO_SIGNAL;
    }

    /**
     * 判断是否突破Macd黄金阻力位
     */
    public static SignalType checkUpCrossGoldSignal(Candlestick c0, Candlestick c1, List<BigDecimal> goldPrices) {
        Collections.sort(goldPrices);
        for (int i = 0; i < goldPrices.size(); i++) {
            BigDecimal goldPrice = goldPrices.get(i);
            if (c0.getClose().compareTo(goldPrice) >= 0
                    && c1.getClose().compareTo(goldPrice) <= 0) {
                return SignalType.BUY;
            }
        }
        Collections.reverse(goldPrices);

        for (int i = 0; i < goldPrices.size(); i++) {
            BigDecimal goldPrice = goldPrices.get(i);
            if (c0.getClose().compareTo(goldPrice) <= 0
                    && c1.getClose().compareTo(goldPrice) >= 0) {
                return SignalType.SELL;
            }
        }
        return SignalType.NO_SIGNAL;
    }

    /**
     * 判断是否回踩Macd黄金支撑位
     */
    public static SignalType checkBackCrossGoldSignal(Candlestick c0, Candlestick c1, List<MacdIndicator.MacdPoint> macdPoints) {
        List<MacdIndicator.MacdPoint> allLatestGoldMACDPoint = MacdIndicator.getAllLatestGoldMACDPoint(macdPoints);

        if (CollectionUtils.isEmpty(allLatestGoldMACDPoint)) {
            return SignalType.NO_SIGNAL;
        }

        List<BigDecimal> goldPrices = new ArrayList<>(allLatestGoldMACDPoint.size() * 2);
        for (MacdIndicator.MacdPoint macdPoint : allLatestGoldMACDPoint) {
            goldPrices.add(macdPoint.getTicker().getLow());
            goldPrices.add(macdPoint.getTicker().getHigh());
        }

        Collections.sort(goldPrices);
        for (int i = 0; i < goldPrices.size(); i++) {
            BigDecimal goldPrice = goldPrices.get(i);
            if (c0.getClose().compareTo(goldPrice) >= 0
                    && c1.getClose().compareTo(goldPrice) >= 0
                    //回踩
                    && c0.getLow().compareTo(goldPrice) <= 0) {
                return SignalType.BUY;
            }
        }
        Collections.reverse(goldPrices);

        for (int i = 0; i < goldPrices.size(); i++) {
            BigDecimal goldPrice = goldPrices.get(i);
            if (c0.getClose().compareTo(goldPrice) <= 0
                    && c1.getClose().compareTo(goldPrice) <= 0
                    //回踩
                    && c0.getHigh().compareTo(goldPrice) >= 0) {
                return SignalType.SELL;
            }
        }
        return SignalType.NO_SIGNAL;
    }

    /**
     * 判断是否macd转折
     */
    public static SignalType checkTurnRoundMacdSignal(List<MacdIndicator.MacdPoint> macdPoints) {
        MacdIndicator.MacdPoint macdPoint0 = macdPoints.get(macdPoints.size() - 1);
        MacdIndicator.MacdPoint macdPoint1 = macdPoints.get(macdPoints.size() - 2);
        MacdIndicator.MacdPoint macdPoint2 = macdPoints.get(macdPoints.size() - 3);

        //持续macd之上，当前发生转折
        if (macdPoint0.getMacd() > 0
                && macdPoint1.getMacd() > 0
                && macdPoint2.getMacd() > 0
                && macdPoint1.getMacd() < macdPoint2.getMacd()
                && macdPoint0.getMacd() > macdPoint1.getMacd()) {
            return SignalType.BUY;
        }

        if (macdPoint0.getMacd() < 0
                && macdPoint1.getMacd() < 0
                && macdPoint2.getMacd() < 0
                && macdPoint1.getMacd() > macdPoint2.getMacd()
                && macdPoint0.getMacd() < macdPoint1.getMacd()) {

            return SignalType.SELL;

        }

        return SignalType.NO_SIGNAL;
    }


    /**
     * 判断MACD之上：趋势上涨
     */
    public static SignalType checkRiseOverMacdSignal(Candlestick c0, Candlestick c1, List<MacdIndicator.MacdPoint> macdPoints) {
        List<MacdIndicator.MacdPoint> allLatestGoldMACDPoint = MacdIndicator.getAllLatestGoldMACDPoint(macdPoints);
        BigDecimal maxGoldHigh = allLatestGoldMACDPoint.stream().map(x -> x.getTicker().getHigh()).max(BigDecimal::compareTo).orElseGet(() -> new BigDecimal(Double.MAX_VALUE));
        BigDecimal minGoldLow = allLatestGoldMACDPoint.stream().map(x -> x.getTicker().getLow()).min(BigDecimal::compareTo).orElseGet(() -> BigDecimal.ZERO);

        MacdIndicator.MacdPoint latestGoldMACDPoint = MacdIndicator.getLatestGoldMACDPoint(macdPoints);
        if (latestGoldMACDPoint == null) {
            return SignalType.NO_SIGNAL;
        }

        if (latestGoldMACDPoint.getMacd() > 0
                && c0.getClose().compareTo(maxGoldHigh) >= 0
                && c0.getClose().compareTo(c0.getOpen()) >= 0
                && c0.getHigh().compareTo(c1.getHigh()) >= 0) {
            return SignalType.BUY;
        }

        if (latestGoldMACDPoint.getMacd() < 0
                && c0.getClose().compareTo(minGoldLow) <= 0
                && c0.getClose().compareTo(c0.getOpen()) <= 0
                && c0.getLow().compareTo(c1.getLow()) <= 0) {
            return SignalType.SELL;
        }

        return SignalType.NO_SIGNAL;
    }


    /**
     * 判断MACD之下：超跌反弹
     */
    public static SignalType checkRiseDownMacdSignal(Candlestick c0, Candlestick c1, List<MacdIndicator.MacdPoint> macdPoints) {
        List<MacdIndicator.MacdPoint> allLatestGoldMACDPoint = MacdIndicator.getAllLatestGoldMACDPoint(macdPoints);
        BigDecimal maxGoldHigh = allLatestGoldMACDPoint.stream().map(x -> x.getTicker().getHigh()).max(BigDecimal::compareTo).orElseGet(() -> new BigDecimal(Double.MAX_VALUE));
        BigDecimal minGoldLow = allLatestGoldMACDPoint.stream().map(x -> x.getTicker().getLow()).min(BigDecimal::compareTo).orElseGet(() -> BigDecimal.ZERO);

        MacdIndicator.MacdPoint latestGoldMACDPoint = MacdIndicator.getLatestGoldMACDPoint(macdPoints);
        if (latestGoldMACDPoint.getMacd() < 0
                && c0.getClose().compareTo(minGoldLow) <= 0
                && c0.getClose().compareTo(c0.getOpen()) >= 0
                && c0.getHigh().compareTo(c1.getHigh()) >= 0) {
            return SignalType.SELL;
        }

        if (latestGoldMACDPoint.getMacd() > 0
                && c0.getClose().compareTo(maxGoldHigh) >= 0
                && c0.getClose().compareTo(c0.getOpen()) <= 0
                && c0.getLow().compareTo(c1.getLow()) <= 0) {
            return SignalType.SELL;
        }

        return SignalType.NO_SIGNAL;

    }


    /**
     * 判断MACD区间：凹突破
     */
    public static SignalType checkRiseBetweenMacdSignal(List<Candlestick> candlesticks, List<MacdIndicator.MacdPoint> macdPoints) {
        MacdIndicator.MacdPoint latestRedGoldMACDPoint = MacdIndicator.getLatestRedGoldMACDPoint(macdPoints);
        MacdIndicator.MacdPoint latestGreenGoldMACDPoint = MacdIndicator.getLatestGreenGoldMACDPoint(macdPoints);

        MacdIndicator.MacdPoint maxGoldMACDPoint = latestRedGoldMACDPoint.getTicker().getLow().compareTo(latestGreenGoldMACDPoint.getTicker().getHigh()) > 0 ? latestRedGoldMACDPoint : latestGreenGoldMACDPoint;
        MacdIndicator.MacdPoint minGoldMACDPoint = latestGreenGoldMACDPoint.getTicker().getLow().compareTo(latestRedGoldMACDPoint.getTicker().getHigh()) > 0 ? latestGreenGoldMACDPoint : latestRedGoldMACDPoint;

        if (maxGoldMACDPoint == null || minGoldMACDPoint == null) {
            return SignalType.NO_SIGNAL;
        }

        Candlestick c0 = candlesticks.get(candlesticks.size() - 1);
        Candlestick c1 = candlesticks.get(candlesticks.size() - 2);

        if (MathUtil.between(c0.getHigh(), minGoldMACDPoint.getTicker().getHigh(), maxGoldMACDPoint.getTicker().getLow())
                //斜侧凹突破
//                && c1.getHigh().compareTo(c2.getHigh()) <= 0
                && c0.getClose().compareTo(c1.getHigh()) > 0) {
            return SignalType.BUY;
        }

        if (MathUtil.between(c0.getHigh(), minGoldMACDPoint.getTicker().getHigh(), maxGoldMACDPoint.getTicker().getLow())
                //非凹突破
//                && c1.getClose().compareTo(MathUtil.min(c2.getHigh(), c3.getHigh())) <= 0
                && c0.getClose().compareTo(c1.getLow()) < 0) {
            return SignalType.SELL;
        }

        return SignalType.NO_SIGNAL;

    }

    /**
     * 回踩
     *
     * @param candlesticks
     * @param goldPrices
     * @return
     */
    public static SignalType checkFallGoldMacdSignal(List<Candlestick> candlesticks, List<BigDecimal> goldPrices) {

        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        Candlestick candlestick1 = candlesticks.get(candlesticks.size() - 2);
        Candlestick candlestick2 = candlesticks.get(candlesticks.size() - 3);
        Candlestick candlestick3 = candlesticks.get(candlesticks.size() - 4);


        /////////////////////////////////     buy     //////////////////////////////////////////
        //回踩1（当日回踩）
        for (BigDecimal goldPrice : goldPrices) {
            if (candlestick0.getClose().compareTo(goldPrice) >= 0
                    && candlestick1.getClose().compareTo(goldPrice) >= 0
                    && candlestick0.getLow().compareTo(goldPrice) <= 0) {
                return SignalType.BUY;
            }
        }

        //回踩2（昨日回踩）
        for (BigDecimal goldPrice : goldPrices) {
            if (candlestick0.getClose().compareTo(goldPrice) >= 0
                    && candlestick1.getClose().compareTo(goldPrice) <= 0
                    && candlestick2.getClose().compareTo(goldPrice) >= 0) {
                return SignalType.BUY;
            }
        }

        //回踩3（昨2日回踩）
        for (BigDecimal goldPrice : goldPrices) {
            if (candlestick0.getClose().compareTo(goldPrice) >= 0
                    && candlestick1.getClose().compareTo(goldPrice) <= 0
                    && candlestick2.getClose().compareTo(goldPrice) <= 0
                    && candlestick3.getClose().compareTo(goldPrice) >= 0) {
                return SignalType.BUY;
            }
        }


        /////////////////////////////////     sell     //////////////////////////////////////////
        for (BigDecimal goldPrice : goldPrices) {
            if (candlestick0.getClose().compareTo(goldPrice) <= 0
                    && candlestick1.getClose().compareTo(goldPrice) <= 0
                    && candlestick0.getHigh().compareTo(goldPrice) >= 0) {
                return SignalType.SELL;
            }
        }

        //回踩2（昨日回踩）
        for (BigDecimal goldPrice : goldPrices) {
            if (candlestick0.getClose().compareTo(goldPrice) <= 0
                    && candlestick1.getClose().compareTo(goldPrice) >= 0
                    && candlestick2.getClose().compareTo(goldPrice) <= 0) {
                return SignalType.SELL;
            }
        }

        //回踩3（昨2日回踩）
        for (BigDecimal goldPrice : goldPrices) {
            if (candlestick0.getClose().compareTo(goldPrice) <= 0
                    && candlestick1.getClose().compareTo(goldPrice) >= 0
                    && candlestick2.getClose().compareTo(goldPrice) >= 0
                    && candlestick3.getClose().compareTo(goldPrice) <= 0) {
                return SignalType.SELL;
            }
        }

        return SignalType.NO_SIGNAL;

    }

    /**
     * 突破黄金位
     *
     * @param candlesticks
     * @param goldPrices
     * @return
     */
    public static SignalType checkCrossGoldMacdSignal(List<Candlestick> candlesticks, List<BigDecimal> goldPrices) {

        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        Candlestick candlestick1 = candlesticks.get(candlesticks.size() - 2);

        /////////////////////////////////     buy     //////////////////////////////////////////
        //突破1
        for (BigDecimal goldPrice : goldPrices) {
            if (candlestick0.getClose().compareTo(goldPrice) >= 0
                    && candlestick1.getClose().compareTo(goldPrice) <= 0) {
                return SignalType.BUY;
            }
        }

        /////////////////////////////////     sell     //////////////////////////////////////////
        //突破2
        for (BigDecimal goldPrice : goldPrices) {
            if (candlestick0.getClose().compareTo(goldPrice) <= 0
                    && candlestick1.getClose().compareTo(goldPrice) >= 0) {
                return SignalType.SELL;
            }
        }

        return SignalType.NO_SIGNAL;

    }

}
