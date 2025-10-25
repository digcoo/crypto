package com.binance.client.enums;

import com.binance.client.model.enums.CandlestickInterval;
import lombok.Getter;

import java.math.BigDecimal;

//@Getter
public enum PeriodTypeEnum {
    MIN5(0.005, 0.0065, CandlestickInterval.FIVE_MINUTES),
    MIN15(0.005, 0.0065, CandlestickInterval.FIFTEEN_MINUTES),
    MIN30(0.005, 0.0065, CandlestickInterval.HALF_HOURLY),
    HOUR1(0.005, 0.0065, CandlestickInterval.HOURLY),

    HOUR4(0.01, 0.015, CandlestickInterval.FOUR_HOURLY),
    DAY(0.015, 0.03, CandlestickInterval.DAILY),
    WEEK(0.02, 0.04, CandlestickInterval.WEEKLY),
    MONTH(0.03, 0.05, CandlestickInterval.MONTHLY),
    ;

    private BigDecimal tiziHeight;

    private BigDecimal tiziZhenFuRate;
    private CandlestickInterval interval;

    PeriodTypeEnum(double height, double zhenfuRate, CandlestickInterval interval) {
        this.tiziHeight = new BigDecimal(height);
        this.tiziZhenFuRate = new BigDecimal(zhenfuRate);
        this.interval = interval;
    }

    public BigDecimal getTiziHeight() {
        return tiziHeight;
    }

    public BigDecimal getTiziZhenFuRate() {
        return tiziZhenFuRate;
    }

    public CandlestickInterval getInterval() {
        return interval;
    }
}
