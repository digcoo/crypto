package com.binance.client.model.market;

import com.alibaba.fastjson.annotation.JSONField;
import com.binance.client.constant.BinanceApiConstants;
import com.binance.client.utils.MathUtil;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@SuperBuilder
@NoArgsConstructor
public class Candlestick {

    private String symbol;

    private Long openTime;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal volume;

    private Long closeTime;

    private BigDecimal quoteAssetVolume;

    private Integer numTrades;

    private BigDecimal takerBuyBaseAssetVolume;

    private BigDecimal takerBuyQuoteAssetVolume;

    private BigDecimal ignore;

    public Long getOpenTime() {
        return openTime;
    }

    public void setOpenTime(Long openTime) {
        this.openTime = openTime;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getZhenFuRate() {
        return this.getHigh().subtract(this.getLow()).divide(this.getLow(), getPrecision() + 1, RoundingMode.DOWN);
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getZhenFuWithSide() {
        return this.getClose().compareTo(this.getOpen()) > 0? getZhenFuRate(): getZhenFuRate().negate();
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getShiTiMax() {
        return MathUtil.max(this.getOpen(), this.getClose());
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getShiTiMin() {
        return MathUtil.min(this.getOpen(), this.getClose());
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getShiTiRate() {
        return this.getClose().subtract(this.getOpen()).divide(this.getOpen(), 4, RoundingMode.DOWN);
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public Long getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Long closeTime) {
        this.closeTime = closeTime;
    }

    public BigDecimal getQuoteAssetVolume() {
        return quoteAssetVolume;
    }

    public void setQuoteAssetVolume(BigDecimal quoteAssetVolume) {
        this.quoteAssetVolume = quoteAssetVolume;
    }

    public Integer getNumTrades() {
        return numTrades;
    }

    public void setNumTrades(Integer numTrades) {
        this.numTrades = numTrades;
    }

    public BigDecimal getTakerBuyBaseAssetVolume() {
        return takerBuyBaseAssetVolume;
    }

    public void setTakerBuyBaseAssetVolume(BigDecimal takerBuyBaseAssetVolume) {
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
    }

    public BigDecimal getTakerBuyQuoteAssetVolume() {
        return takerBuyQuoteAssetVolume;
    }

    public void setTakerBuyQuoteAssetVolume(BigDecimal takerBuyQuoteAssetVolume) {
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
    }

    public BigDecimal getIgnore() {
        return ignore;
    }

    public void setIgnore(BigDecimal ignore) {
        this.ignore = ignore;
    }

    public Candlestick setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getCloseTimeStr() {
        return DateFormatUtils.format(this.closeTime, "yyyy-MM-dd HH:mm:ss");
    }

    public String getOpenTimeStr() {
        return DateFormatUtils.format(this.openTime, "yyyy-MM-dd HH:mm:ss");
    }

    public int getPrecision() {
//        String closeStr = this.close.toString();
//        return closeStr.substring(closeStr.indexOf(".")).length();
        return this.close.precision();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE).append("openTime", openTime)
                .append("open", open).append("high", high).append("low", low).append("close", close)
                .append("volume", volume).append("closeTime", closeTime).append("quoteAssetVolume", quoteAssetVolume)
                .append("numTrades", numTrades).append("takerBuyBaseAssetVolume", takerBuyBaseAssetVolume)
                .append("takerBuyQuoteAssetVolume", takerBuyQuoteAssetVolume).append("ignore", ignore).toString();
    }

}
