package com.digcoo.fitech.common.model;


import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.util.MathUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Candlestick implements Serializable {
    private String symbol;
    private String name;
    private boolean st;
    private CandlestickPeriod period;
    private long timestamp;
    private long openTime;
    private long closeTime;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;
    private BigDecimal amount;
    private int numberOfTrades;
    private BigDecimal takerBuyBaseAssetVolume;
    private BigDecimal takerBuyQuoteAssetVolume;
    private boolean isClosed;
    private BigDecimal lastClose;
    private BigDecimal changeRate;
    private BigDecimal shockRate;
    private BigDecimal turnoverRate;   //换手率
    private OrderBook orderBook;

    @JsonIgnore
    public String getOpenTimeStr() {
        return DateFormatUtils.format(this.openTime, "yyyy-MM-dd HH:mm:ss");
    }


    @JsonIgnore
    public BigDecimal getShiTiMax() {
        return MathUtil.max(this.open, this.close);
    }

    @JsonIgnore
    public BigDecimal getShiTiMin() {
        return MathUtil.min(this.open, this.close);
    }

}
