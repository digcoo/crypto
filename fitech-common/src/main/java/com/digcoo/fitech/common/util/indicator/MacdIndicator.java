package com.digcoo.fitech.common.util.indicator;

import com.digcoo.fitech.common.model.Candlestick;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MacdIndicator {

    public static List<MacdPoint> calculateMacd(List<Candlestick> tickers) {
        List<BigDecimal> closes = tickers.stream().map(Candlestick::getClose).collect(Collectors.toList());
        return calculateMacd(tickers, closes);
    }

    public static MacdPoint getLatestGoldMACDPoint(List<MacdPoint> macdPoints) {
        MacdPoint latestGoldMacdPoint = null;
        for (int i = macdPoints.size() - 1; i >= 0; i--) {
            if (macdPoints.get(i).isIfGoldCross()) {
                latestGoldMacdPoint = macdPoints.get(i);
                break;
            }
        }
        return latestGoldMacdPoint;
    }

    public static MacdPoint getLatestGreenGoldMACDPoint(List<MacdPoint> macdPoints) {
        MacdPoint latestGoldMacdPoint = null;
        for (int i = macdPoints.size() - 1; i >= 0; i--) {
            if (macdPoints.get(i).isIfGreenGoldCross()) {
                latestGoldMacdPoint = macdPoints.get(i);
                break;
            }
        }
        return latestGoldMacdPoint;
    }

    public static Pair<MacdPoint, MacdPoint> getLatestGreenGoldMACDPointPair(List<MacdPoint> macdPoints) {
        MacdPoint latestGoldMacdPoint = null;
        MacdPoint latestGoldMacdPoint2 = null;
        int offset = 3;
        for (int i = macdPoints.size() - 1; i >= 0; i--) {
            if (macdPoints.get(i).isIfGreenGoldCross()) {
                latestGoldMacdPoint = macdPoints.get(i);

                //相邻MacdPoint
                latestGoldMacdPoint2 = latestGoldMacdPoint;
                for (int j = i-1; j >= 0 && j > i - 1 - offset ; j--) {
                    MacdPoint macdPoint = macdPoints.get(j);
                    if (macdPoint.getTicker().getClose().compareTo(macdPoint.getTicker().getOpen()) < 0
                            && macdPoint.getTicker().getLow().compareTo(latestGoldMacdPoint2.getTicker().getLow()) < 0) {
                        latestGoldMacdPoint2 = macdPoint;
                    }
                }

                break;
            }
        }

        if (latestGoldMacdPoint != null) {
            return Pair.of(latestGoldMacdPoint, latestGoldMacdPoint2);
        }
        return null;
    }

    public static MacdPoint getLatestRedGoldMACDPoint(List<MacdPoint> macdPoints) {
        MacdPoint latestGoldMacdPoint = null;
        for (int i = macdPoints.size() - 1; i >= 0; i--) {
            if (macdPoints.get(i).isIfRedGoldCross()) {
                latestGoldMacdPoint = macdPoints.get(i);
                break;
            }
        }
        return latestGoldMacdPoint;
    }


    public static Pair<MacdPoint, MacdPoint> getLatestRedGoldMACDPointPair(List<MacdPoint> macdPoints) {
        MacdPoint latestGoldMacdPoint = null;
        MacdPoint latestGoldMacdPoint2 = null;
        int offset = 3;
        for (int i = macdPoints.size() - 1; i >= 0; i--) {
            if (macdPoints.get(i).isIfRedGoldCross()) {
                latestGoldMacdPoint = macdPoints.get(i);

                //相邻MacdPoint
                latestGoldMacdPoint2 = latestGoldMacdPoint;
                for (int j = i-1; j >= 0 && j > i - 1 - offset ; j--) {
                    MacdPoint macdPoint = macdPoints.get(j);
                    if ((macdPoint.getTicker().getClose().compareTo(macdPoint.getTicker().getOpen()) > 0
                                || macdPoint.getTicker().getClose().compareTo(latestGoldMacdPoint.getTicker().getClose()) > 0)
                            && macdPoint.getTicker().getHigh().compareTo(latestGoldMacdPoint2.getTicker().getHigh()) > 0) {
                        latestGoldMacdPoint2 = macdPoint;
                    }
                }

                break;
            }
        }

        if (latestGoldMacdPoint != null) {
            return Pair.of(latestGoldMacdPoint, latestGoldMacdPoint2);
        }
        return null;
    }

    public static List<MacdPoint> getAllLatestGoldMACDPoint(List<MacdPoint> macdPoints) {
        MacdPoint latestRedGoldMACDPoint = getLatestRedGoldMACDPoint(macdPoints);
        MacdPoint latestGreenGoldMACDPoint = getLatestGreenGoldMACDPoint(macdPoints);
        return Lists.newArrayList(latestRedGoldMACDPoint, latestGreenGoldMACDPoint)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 校验：最近红金叉
     * @param macdPoints
     * @return
     */
    public static boolean checkLatestRedGold(List<MacdPoint> macdPoints) {
        MacdIndicator.MacdPoint latestGoldMACDPoint = getLatestGoldMACDPoint(macdPoints);
        return latestGoldMACDPoint != null && latestGoldMACDPoint.isIfRedGoldCross();
    }

    /**
     * 校验：最近绿金叉
     * @param macdPoints
     * @return
     */
    public static boolean checkLatestGreenGold(List<MacdPoint> macdPoints) {
        MacdIndicator.MacdPoint latestGoldMACDPoint = getLatestGoldMACDPoint(macdPoints);
        return latestGoldMACDPoint != null && latestGoldMACDPoint.isIfGreenGoldCross();
    }

    /**
     * 校验：红金叉高于绿金叉
     * @param macdPoints
     * @return
     */
    public static boolean checkRedGoldGreatThanGreenGold(List<MacdPoint> macdPoints) {
        MacdPoint latestRedGoldMACDPoint = getLatestRedGoldMACDPoint(macdPoints);
        MacdPoint latestGreenGoldMACDPoint = getLatestGreenGoldMACDPoint(macdPoints);

        if (latestRedGoldMACDPoint != null &&
                (latestGreenGoldMACDPoint == null || latestRedGoldMACDPoint.getDif() > latestGreenGoldMACDPoint.getDif())) {
            return true;
        }

        return false;
    }

    /**
     * 校验：绿金叉高于红金叉
     * @param macdPoints
     * @return
     */
    public static boolean checkGreenGoldGreatThanRedGold(List<MacdPoint> macdPoints) {
        MacdPoint latestRedGoldMACDPoint = getLatestRedGoldMACDPoint(macdPoints);
        MacdPoint latestGreenGoldMACDPoint = getLatestGreenGoldMACDPoint(macdPoints);

        if (latestGreenGoldMACDPoint != null &&
                (latestRedGoldMACDPoint == null || latestGreenGoldMACDPoint.getDif() > latestRedGoldMACDPoint.getDif())) {
            return true;
        }

        return false;
    }

    // 计算 EMA 指数移动平均线
    private static double calculateEMA(double previousEMA, double currentPrice, int period) {
        double multiplier = 2.0 / (period + 1);
        return (currentPrice - previousEMA) * multiplier + previousEMA;
    }

    // 计算 MACD 指标
    private static List<MacdPoint> calculateMacd(List<Candlestick> tickers, List<BigDecimal> closes) {
        if (closes == null || closes.size() < 10) {
//            throw new IllegalArgumentException("数据点不足，至少需要26个数据点来计算 MACD");
            return Collections.emptyList();
        }

        List<MacdPoint> macdPoints = new ArrayList<>(closes.size());

        double ema12 = closes.get(0).doubleValue();
        double ema26 = closes.get(0).doubleValue();
        double dea = 0;

        for (int i = 0; i < closes.size(); i++) {
            double price = closes.get(i).doubleValue();

            // 计算 12 日 EMA 和 26 日 EMA
            if (i == 0) {
                ema12 = price;
                ema26 = price;
            } else {
                ema12 = calculateEMA(ema12, price, 12);
                ema26 = calculateEMA(ema26, price, 26);
            }

            double dif = ema12 - ema26; // 计算 DIF
            dea = calculateEMA(dea, dif, 9); // 计算 DEA
            double macd = 2 * (dif - dea); // 计算 MACD 柱状图


            MacdPoint.MacdPointBuilder macdPointBuilder = MacdPoint.builder()
                    .ticker(tickers.get(i))
                    .dif(dif)
                    .dea(dea)
                    .macd(macd)
                    .ifOver(false)
                    .ifGoldCross(false);
            ;
            if (macd > 0) {
                macdPointBuilder.ifOver(true);
            }

            if (i > 0) {
                if (macdPoints.get(i - 1).getMacd() < 0 && macd > 0) {
                    macdPointBuilder.ifGoldCross(true);
                    macdPointBuilder.ifGreenGoldCross(false);
                } else if (macdPoints.get(i - 1).getMacd() > 0 && macd < 0) {
                    macdPointBuilder.ifGoldCross(true);
                    macdPointBuilder.ifGreenGoldCross(true);
                }

            }

            macdPoints.add(macdPointBuilder.build());
        }

        return macdPoints;
    }


    @Data
    @Builder
    public static class MacdPoint {
        private Candlestick ticker;
        private double dif;
        private double dea;
        private double macd;
        private boolean ifOver;
        private boolean ifGoldCross;
        private boolean ifGreenGoldCross;

        public boolean isIfGreenGoldCross() {
            return ifGoldCross && ifGreenGoldCross;
        }

        public boolean isIfRedGoldCross() {
            return ifGoldCross && !ifGreenGoldCross;
        }

        private String getOpenTimeStr() {
            return this.ticker.getOpenTimeStr();
        }

    }
}
