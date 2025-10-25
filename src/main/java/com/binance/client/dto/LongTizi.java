package com.binance.client.dto;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.market.Candlestick;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Data
public class LongTizi {

    private String symbol;

    private List<Candlestick> trades;

    private PeriodTypeEnum periodTypeEnum;

    private BigDecimal tprice;

    public LongTizi(String symbol, List<Candlestick> trades, PeriodTypeEnum periodTypeEnum, BigDecimal tprice) {
        this.symbol = symbol;
        this.trades = trades;
        this.periodTypeEnum = periodTypeEnum;
        this.tprice = tprice;
    }

    public String getSymbol(){
        return symbol;
    }

    public int precision() {
        return this.trades.get(0).getClose().precision();
    }

    public BigDecimal getOpen() {
        return this.trades.stream().map(x -> x.getOpen()).min(Comparator.comparing(BigDecimal::doubleValue)).get();
    }

    public BigDecimal getHigh() {
        return this.trades.stream().map(x -> x.getHigh()).max(Comparator.comparing(BigDecimal::doubleValue)).get();
    }

    public BigDecimal getLow() {
        return this.trades.stream().map(x -> x.getLow()).max(Comparator.comparing(BigDecimal::doubleValue)).get();
    }

    public BigDecimal getClose() {
        return this.trades.stream().map(x -> x.getClose()).max(Comparator.comparing(BigDecimal::doubleValue)).get();
    }

    public BigDecimal getShiTiRate() {
        return getClose().subtract(getOpen()).divide(getOpen(), precision(), RoundingMode.HALF_UP);
    }

    public BigDecimal getZhenFuRate() {
        return getHigh().subtract(getLow()).divide(getLow(), precision(), RoundingMode.HALF_UP);
    }

    public int size() {
        return this.trades.size();
    }

    public Candlestick getStartTrade() {
        return this.trades.get(0);
    }

    public Candlestick getEndTrade() {
        return this.trades.get(size() - 1);
    }

    public Long getStartTime() {
        return getStartTrade().getOpenTime();
    }

    public Long getEndTime() {
        return getEndTrade().getCloseTime();
    }

    public boolean contain(Candlestick candlestick) {
        if(candlestick.getCloseTime() >= getStartTime() && candlestick.getCloseTime() <= getEndTime()) {
            return true;
        }
        return false;
    }
}
