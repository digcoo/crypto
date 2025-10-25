package com.digcoo.fitech.stock;


import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.stock.core.HistoricalDataHandler;
import com.digcoo.fitech.stock.core.RealTimeDataHandler;
import com.digcoo.fitech.stock.ws.SinaWebsocketClientImpl;
import com.digcoo.fitech.stock.ws.base.StockWebsocketClient;

import java.net.http.HttpClient;
import java.util.List;

public class DataHandlerTest {

    public static void main(String[] args) throws InterruptedException {
        // 初始化客户端
        HttpClient restClient = HttpClient.newHttpClient();
        StockWebsocketClient wsClient = new SinaWebsocketClientImpl(null);

        // 创建数据处理器
        HistoricalDataHandler historicalData = new HistoricalDataHandler(null, null);
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
    }

}
