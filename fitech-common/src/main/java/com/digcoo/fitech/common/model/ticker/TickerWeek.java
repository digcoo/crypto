package com.digcoo.fitech.common.model.ticker;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class TickerWeek {
    private String symbol;
    private BigDecimal priceChange;
    private BigDecimal priceChangePercent;
    private BigDecimal weightedAvgPrice;
    private BigDecimal prevClosePrice;
    private BigDecimal lastPrice;
    private BigDecimal lastQty;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal volume;
    private BigDecimal quoteVolume;
    private long openTime;
    private long closeTime;
    private long firstId;
    private long lastId;
    private int count;


}
