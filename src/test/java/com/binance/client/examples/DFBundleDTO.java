package com.binance.client.examples;

import com.binance.client.model.market.Candlestick;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;


@Data
public class DFBundleDTO {

    String code;
    DFDTO mlines;
    DFDTO wlines;
    DFDTO dlines;
    DFDTO h4lines;
    DFDTO m30lines;
    DFDTO m5lines;

    public DFBundleDTO(String code, List<Candlestick> monthCandlesticks, List<Candlestick> weekCandlesticks,
                       List<Candlestick> dayCandlesticks, List<Candlestick> hour4Candlesticks, List<Candlestick> min30Candlesticks, List<Candlestick> min5Candlesticks) {
        this.code = code;
        this.mlines = convertDFDTO(monthCandlesticks);
        this.wlines = convertDFDTO(weekCandlesticks);;
        this.dlines = convertDFDTO(dayCandlesticks);;
        this.h4lines = convertDFDTO(hour4Candlesticks);;
        this.m30lines = convertDFDTO(min30Candlesticks);;
        this.m5lines = convertDFDTO(min5Candlesticks);;
    }

    private DFDTO convertDFDTO(List<Candlestick> candlesticks) {
        DFDTO dfdto = new DFDTO();
        dfdto.setDate(candlesticks.stream().map(x -> x.getCloseTimeStr()).collect(Collectors.toList()).toArray(new String[candlesticks.size()]));
        dfdto.setOpen(candlesticks.stream().map(x -> x.getOpen().doubleValue()).collect(Collectors.toList()).toArray(new Double[candlesticks.size()]));
        dfdto.setHigh(candlesticks.stream().map(x -> x.getHigh().doubleValue()).collect(Collectors.toList()).toArray(new Double[candlesticks.size()]));
        dfdto.setLow(candlesticks.stream().map(x -> x.getLow().doubleValue()).collect(Collectors.toList()).toArray(new Double[candlesticks.size()]));
        dfdto.setClose(candlesticks.stream().map(x -> x.getClose().doubleValue()).collect(Collectors.toList()).toArray(new Double[candlesticks.size()]));
        return dfdto;
    }

    public static class DFDTO {
        private String[] Date;
        private Double[] Open;
        private Double[] High;
        private Double[] Low;
        private Double[] Close;

        public String[] getDate() {
            return Date;
        }

        public void setDate(String[] date) {
            Date = date;
        }

        public Double[] getOpen() {
            return Open;
        }

        public void setOpen(Double[] open) {
            Open = open;
        }

        public Double[] getHigh() {
            return High;
        }

        public void setHigh(Double[] high) {
            High = high;
        }

        public Double[] getLow() {
            return Low;
        }

        public void setLow(Double[] low) {
            Low = low;
        }

        public Double[] getClose() {
            return Close;
        }

        public void setClose(Double[] close) {
            Close = close;
        }
    }
}
