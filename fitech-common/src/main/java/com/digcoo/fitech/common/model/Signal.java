package com.digcoo.fitech.common.model;


import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.enums.OrderSide;
import com.digcoo.fitech.common.enums.SignalType;
import com.digcoo.fitech.common.enums.StrategyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class Signal {
    private String symbol;
    private CandlestickPeriod period;
    private OrderSide orderSide;
    private StrategyType strategyType;
    private SignalType signalType;

    //委托价格
    private BigDecimal delegatePrice;
    //委托数量
    private BigDecimal delegateQty;

    //当前价格
    private BigDecimal currentPrice;

    //信号触发时间
    private long triggerTimestamp;

    //止盈价格
    private BigDecimal tpPrice;

    //止盈价格
    private BigDecimal slPrice;


    public static final Signal NO_SIGNAL = Signal.builder()
            .build();

    public String getTimestampStr() {
        return DateFormatUtils.format(triggerTimestamp, "yyyy-MM-dd HH:mm:ss");
    }
}
