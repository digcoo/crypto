package com.binance.client.utils;

import com.binance.client.enums.SideTypeEnum;
import com.binance.client.model.market.Candlestick;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public final class IndicatorCaculater {

    public static boolean checkOverMACD(List<Candlestick> trades, SideTypeEnum sideTypeEnum) {

        String symbol = trades.get(0).getSymbol();
        BaseBarSeries barSeries = new BaseBarSeries(symbol);

        for (Candlestick candlestick : trades) {
            ZonedDateTime dateZone = new Date(candlestick.getOpenTime()).toInstant().atZone(ZoneId.systemDefault());
            if (SideTypeEnum.LONG == sideTypeEnum) {
                barSeries.addBar(dateZone, candlestick.getOpen(), candlestick.getHigh(), candlestick.getLow(), candlestick.getClose());
            }else {
                barSeries.addBar(dateZone, candlestick.getOpen().negate(), candlestick.getHigh().negate(), candlestick.getLow().negate(), candlestick.getClose().negate());
            }
        }

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(barSeries);
        MACDIndicator macdIndicator = new MACDIndicator(closePriceIndicator, 12, 26);
        EMAIndicator emaIndicator = new EMAIndicator(macdIndicator, 9);

        double macdValue = macdIndicator.getValue(barSeries.getBarCount() - 1).doubleValue();
        double signalValue = emaIndicator.getValue(barSeries.getBarCount() - 1).doubleValue();

        return macdValue >= signalValue;
    }

    public static BigDecimal calMA(List<Candlestick> trades, SideTypeEnum sideTypeEnum, int maValue) {
        String symbol = trades.get(0).getSymbol();
        BaseBarSeries barSeries = new BaseBarSeries(symbol);

        for (Candlestick candlestick : trades) {
            ZonedDateTime dateZone = new Date(candlestick.getOpenTime()).toInstant().atZone(ZoneId.systemDefault());
            if (SideTypeEnum.LONG == sideTypeEnum) {
                barSeries.addBar(dateZone, candlestick.getOpen(), candlestick.getHigh(), candlestick.getLow(), candlestick.getClose());
            }else {
                barSeries.addBar(dateZone, candlestick.getOpen().negate(), candlestick.getHigh().negate(), candlestick.getLow().negate(), candlestick.getClose().negate());
            }
        }

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(barSeries);
        return BigDecimal.valueOf(new SMAIndicator(closePriceIndicator, maValue).getValue(barSeries.getBarCount() - 1).doubleValue());
    }


    public static BigDecimal calMAMax(List<Candlestick> trades, SideTypeEnum sideTypeEnum) {
        String symbol = trades.get(0).getSymbol();
        BaseBarSeries barSeries = new BaseBarSeries(symbol);

        for (Candlestick candlestick : trades) {
            ZonedDateTime dateZone = new Date(candlestick.getOpenTime()).toInstant().atZone(ZoneId.systemDefault());
            if (SideTypeEnum.LONG == sideTypeEnum) {
                barSeries.addBar(dateZone, candlestick.getOpen(), candlestick.getHigh(), candlestick.getLow(), candlestick.getClose());
            }else {
                barSeries.addBar(dateZone, candlestick.getOpen().negate(), candlestick.getHigh().negate(), candlestick.getLow().negate(), candlestick.getClose().negate());
            }
        }

        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(barSeries);

        List<BigDecimal> maValues = Arrays.asList(
                BigDecimal.valueOf(new SMAIndicator(closePriceIndicator, 7).getValue(barSeries.getBarCount() - 1).doubleValue()),
                BigDecimal.valueOf(new SMAIndicator(closePriceIndicator, 14).getValue(barSeries.getBarCount() - 1).doubleValue()),
                BigDecimal.valueOf(new SMAIndicator(closePriceIndicator, 28).getValue(barSeries.getBarCount() - 1).doubleValue()),
                BigDecimal.valueOf(new SMAIndicator(closePriceIndicator, 56).getValue(barSeries.getBarCount() - 1).doubleValue())
                );

        return MathUtil.max(maValues);
    }

}
