package com.binance.client.utils;

import com.binance.client.enums.SideTypeEnum;
import com.binance.client.model.market.Candlestick;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CheckValidator {
    public static boolean checkMACrossMA(List<Candlestick> candlesticks, int crossMA, int baseMA, SideTypeEnum sideTypeEnum) {
        List<Candlestick> lastCandlesticks = candlesticks.subList(0, candlesticks.size() - 1);
        BigDecimal lastBaseMA = IndicatorCaculater.calMA(lastCandlesticks, sideTypeEnum, baseMA);
        BigDecimal lastCrossMA = IndicatorCaculater.calMA(lastCandlesticks, sideTypeEnum, crossMA);

        BigDecimal curBaseMA = IndicatorCaculater.calMA(candlesticks, sideTypeEnum, baseMA);
        BigDecimal curCrossMA = IndicatorCaculater.calMA(candlesticks, sideTypeEnum, crossMA);
        if (sideTypeEnum == SideTypeEnum.LONG) {
            return lastCrossMA.compareTo(lastBaseMA) < 0 && curCrossMA.compareTo(curBaseMA) > 0;
        }else {
            return lastCrossMA.compareTo(lastBaseMA) > 0 && curCrossMA.compareTo(curBaseMA) < 0;
        }
    }

    public static boolean checkOverMA(List<Candlestick> candlesticks, List<Integer> MAs, SideTypeEnum sideTypeEnum) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        boolean flag = true;
        for (Integer MA : MAs) {
            if (sideTypeEnum == SideTypeEnum.LONG) {
                flag = flag && candlestick0.getClose().compareTo(IndicatorCaculater.calMA(candlesticks, sideTypeEnum, MA)) > 0;
            }else {
                flag = flag && candlestick0.getClose().negate().compareTo(IndicatorCaculater.calMA(candlesticks, sideTypeEnum, MA)) > 0;
            }
        }
        return flag;
    }

    public static boolean checkMAGongzhen(List<Candlestick> candlesticks, List<Integer> MAs, SideTypeEnum sideTypeEnum) {
        List<BigDecimal> MAValues = MAs.stream().map(x -> IndicatorCaculater.calMA(candlesticks, sideTypeEnum, x)).collect(Collectors.toList());
        return IntStream.range(0, MAValues.size() - 1).allMatch(i -> MAValues.get(i).compareTo(MAValues.get(i + 1)) <= 0);
    }

    public static boolean checkShangyi(List<Candlestick> candlesticks, SideTypeEnum sideTypeEnum) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        Candlestick candlestick1 = candlesticks.get(candlesticks.size() - 2);
        if (sideTypeEnum == SideTypeEnum.LONG) {
            return candlestick0.getHigh().compareTo(candlestick1.getHigh()) > 0 && candlestick0.getClose().compareTo(candlestick1.getClose()) > 0;
        }else {
            return candlestick0.getHigh().negate().compareTo(candlestick1.getHigh().negate()) > 0 && candlestick0.getClose().negate().compareTo(candlestick1.getClose().negate()) > 0;
        }
    }
}
