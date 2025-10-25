package com.digcoo.fitech.stock.core.callback;

import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.ws.PriceUpdateCallback;
import com.digcoo.fitech.stock.core.RealTimeDataHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TickerWeekWSCallback implements PriceUpdateCallback {

    private final String symbol;
    private final CandlestickPeriod period;

    RealTimeDataHandler realTimeDataHandler;

    public TickerWeekWSCallback(
            String symbol,
            CandlestickPeriod period,
            RealTimeDataHandler realTimeDataHandler) {
        this.symbol = symbol;
        this.period = period;
        this.realTimeDataHandler = realTimeDataHandler;
    }

    @Override
    public void onReceive(Candlestick realtimeCandlestick) {

        log.info("TickerWeekWSCallback onReceive: {}", realtimeCandlestick);

    }


}
