package com.binance.client.model.market;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.enums.MAType;
import com.binance.client.model.enums.PeriodType;
import com.binance.client.utils.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MACross {

    PeriodTypeEnum periodType;

    MALine ma1;
    MALine ma2;

    BigDecimal crossPointY;

    Long openTime;


//    public BigDecimal getCrossPrice(){
//        return MathUtil.calCrossPointY(ma1, ma2);
//    }

    public boolean isContain(Candlestick candlestick) {
        return this.openTime.equals(candlestick.getOpenTime());
    }

    public boolean isBefore(Candlestick candlestick) {
        return this.openTime < candlestick.getOpenTime();
    }

    public boolean isAfter(Candlestick candlestick) {
        return this.openTime > candlestick.getOpenTime();
    }

    public String getOpenTimeStr() {
        return DateFormatUtils.format(this.openTime, "yyyy-MM-dd HH:mm:ss");
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class MALine {
        PeriodTypeEnum periodType;
        MAType maType;
        Pair<BigDecimal, BigDecimal> MALine;
        Long openTime;

        public String getOpenTimeStr() {
            return DateFormatUtils.format(this.openTime, "yyyy-MM-dd HH:mm:ss");
        }

    }
}
