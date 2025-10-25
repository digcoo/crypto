package com.digcoo.fitech.crypto;


import com.binance.connector.futures.client.FuturesClient;
import com.binance.connector.futures.client.WebsocketClient;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;
import com.digcoo.fitech.common.constants.GlobalConstants;
import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.crypto.core.HistoricalDataHandler;
import com.digcoo.fitech.crypto.core.RealTimeDataHandler;

import java.util.List;

public class DataHandlerTest {

    public static void main(String[] args) throws InterruptedException {
        // 初始化客户端
        FuturesClient restClient = new UMFuturesClientImpl(GlobalConstants.API_KEY, GlobalConstants.SECRET_KEY);
        WebsocketClient wsClient = new UMWebsocketClientImpl();

        // 创建数据处理器
        HistoricalDataHandler historicalData = new HistoricalDataHandler(restClient);
        RealTimeDataHandler realTimeData = new RealTimeDataHandler(wsClient);

        String symbol = "BTCUSDT";
        CandlestickPeriod period = CandlestickPeriod.ONE_MINUTE;

        // 获取历史数据示例
        List<Candlestick> candles = historicalData.getCandles(symbol, period, 100);
        System.out.println("Fetched " + candles.size() + " historical candles");

        // 订阅实时数据示例
//        realTimeData.subscribeTrade(symbol);
        realTimeData.subscribeCandlestick(symbol, period);
//        realTimeData.subscribeTicker24H(symbol);

        // 等待接收实时数据
        Thread.sleep(60000 * 2);

        // 获取最新数据示例
        Candlestick latestCandle = realTimeData.getLatestCandle(symbol, period);
        System.out.println("Latest candle close price: " + latestCandle.getClose());

        // 取消订阅
        realTimeData.unsubscribe(symbol + "@kline_" + period);
        realTimeData.unsubscribe(symbol + "@ticker");
    }

}
