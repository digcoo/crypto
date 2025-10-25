package com.digcoo.fitech.crypto.core.callback;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.binance.connector.futures.client.utils.WebSocketCallback;
import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.crypto.core.RealTimeDataHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class CandlestickWSCallback implements WebSocketCallback {

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
    public void onReceive(String response) {
//        log.info("CandlestickWSCallback onReceive: {}", response);

        // 解析K线数据
        JSONObject kline = JSON.parseObject(response).getJSONObject("k");
        Candlestick realtimeCandlestick = new Candlestick();
        realtimeCandlestick.setOpenTime(kline.getLong("t"));
        realtimeCandlestick.setOpen(kline.getBigDecimal("o"));
        realtimeCandlestick.setHigh(kline.getBigDecimal("h"));
        realtimeCandlestick.setLow(kline.getBigDecimal("l"));
        realtimeCandlestick.setClose(kline.getBigDecimal("c"));
        realtimeCandlestick.setVolume(kline.getBigDecimal("v"));
        realtimeCandlestick.setCloseTime(kline.getLong("T"));

        realTimeDataHandler.updateRealTimeKline(realtimeCandlestick);
        realTimeDataHandler.notifyCandleSubscribers(symbol, period, realtimeCandlestick);

    }


}
