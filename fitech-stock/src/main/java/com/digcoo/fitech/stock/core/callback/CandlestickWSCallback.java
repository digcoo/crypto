package com.digcoo.fitech.stock.core.callback;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.binance.connector.futures.client.utils.WebSocketCallback;
import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.ws.PriceUpdateCallback;
import com.digcoo.fitech.stock.core.RealTimeDataHandler;
import com.digcoo.fitech.stock.ws.base.StockWebsocketClient;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CandlestickWSCallback implements PriceUpdateCallback {

    private final String symbol;
    private final CandlestickPeriod period;

    RealTimeDataHandler realTimeDataHandler;

    public CandlestickWSCallback(
            String symbol,
            CandlestickPeriod period,
            RealTimeDataHandler realTimeDataHandler) {
        this.symbol = symbol;
        this.period = period;
        this.realTimeDataHandler = realTimeDataHandler;
    }

    @Override
    public void onReceive(Candlestick realtimeCandlestick) {
//        log.info("CandlestickWSCallback onReceive: {}", response);
        realtimeCandlestick.setPeriod(period);

        realTimeDataHandler.updateRealTimeKline(realtimeCandlestick);
        realTimeDataHandler.notifyCandleSubscribers(symbol, period, realtimeCandlestick);

    }


}
