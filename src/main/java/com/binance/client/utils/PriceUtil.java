package com.binance.client.utils;

import com.binance.client.model.market.Candlestick;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class PriceUtil {

    public static boolean isSameTrend(List<Candlestick> candlesticks) {
        boolean red = candlesticks.get(0).getClose().compareTo(candlesticks.get(0).getOpen()) > 0;
        for (Candlestick candlestick : candlesticks) {
            if (red != (candlestick.getClose().compareTo(candlestick.getOpen()) > 0)) {
                return false;
            }
        }
        return true;
    }

}
