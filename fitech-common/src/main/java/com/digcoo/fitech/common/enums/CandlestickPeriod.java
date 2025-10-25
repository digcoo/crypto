package com.digcoo.fitech.common.enums;

import lombok.Getter;

/**
 * 1min, 5min, 15min, 30min, 60min, 1day, 1mon, 1week, 1year
 */

@Getter
public enum CandlestickPeriod {
    ONE_MINUTE("1m", "1m"),
    THREE_MINUTES("3m", "3m"),
    FIVE_MINUTES("5m", "5m"),
    FIFTEEN_MINUTES("15m", "15m"),
    HALF_HOURLY("30m", "30m"),
    HOURLY("1h", "1h"),
    TWO_HOURLY("2h", "2h"),
    FOUR_HOURLY("4h", "4h"),
    SIX_HOURLY("6h", "6h"),
    EIGHT_HOURLY("8h", "8h"),
    TWELVE_HOURLY("12h", "12h"),
    DAILY("1d", "day"),
    THREE_DAILY("3d", "3d"),
    WEEKLY("1w", "week"),
    MONTHLY("1M", "month"),
    YEARLY("1Y", "year");

    private final String coinPeriod;
    private final String stockPeriod;

    CandlestickPeriod(String coinPeriod, String stockPeriod) {
        this.coinPeriod = coinPeriod;
        this.stockPeriod = stockPeriod;
    }

    public static CandlestickPeriod toByStockPeriod(String period) {
        for (CandlestickPeriod candlestickPeriod : CandlestickPeriod.values()) {
            if (candlestickPeriod.getStockPeriod().equals(period)) {
                return candlestickPeriod;
            }
        }
        throw new IllegalArgumentException("period is not supported");
    }
}
