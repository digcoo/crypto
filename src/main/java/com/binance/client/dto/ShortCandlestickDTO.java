package com.binance.client.dto;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.market.ShortCandlestickMA;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Slf4j
public class ShortCandlestickDTO {

    private String symbol;

    private PeriodTypeEnum periodTypeEnum;

    List<ShortCandlestickMA> candlestickMAS;     //按照时间升序

    public ShortCandlestickMA getLastCandlestick(int leftOffset) {
        if (getSize() + leftOffset - 1 >= 0){
            return candlestickMAS.get(getSize() + leftOffset - 1);
        }
        return null;
    }

    public List<ShortCandlestickMA> getLastCandlesticks(int num) {
        if (getSize() >= num){
            return candlestickMAS.subList(getSize() - num, candlestickMAS.size());
        }
        return candlestickMAS;
    }

    public ShortCandlestickMA getCurrentCandlestick(){
        return getLastCandlestick(0);
    }

    public int getSize() {
        return this.candlestickMAS == null? 0 : this.candlestickMAS.size();
    }

    public BigDecimal getZhenFuRate() {
        ShortCandlestickMA currentCandlestick = getCurrentCandlestick();
        return currentCandlestick.getHigh().subtract(currentCandlestick.getLow()).divide(currentCandlestick.getLow(), 3, RoundingMode.HALF_UP);
    }

}
