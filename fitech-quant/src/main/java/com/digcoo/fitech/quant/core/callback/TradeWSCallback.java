package com.digcoo.fitech.quant.core.callback;

import com.alibaba.fastjson2.JSON;
import com.binance.connector.futures.client.utils.WebSocketCallback;
import com.digcoo.fitech.common.model.Trade;
import com.digcoo.fitech.quant.core.RealTimeDataHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TradeWSCallback implements WebSocketCallback {

    private final String symbol;
    RealTimeDataHandler realTimeDataHandler;

    public TradeWSCallback(
            String symbol,
            RealTimeDataHandler realTimeDataHandler) {
        this.symbol = symbol;
        this.realTimeDataHandler = realTimeDataHandler;
    }

    @Override
    public void onReceive(String response) {
        log.info("TradeWSCallback onReceive: {}", response);

        Trade realtimeTrade = JSON.parseObject(response, Trade.class);

        realTimeDataHandler.updateRealTimeTrade(realtimeTrade);
        realTimeDataHandler.notifyTradeSubscribers(symbol, realtimeTrade);

    }


}
