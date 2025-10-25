package com.digcoo.fitech.quant.core;

import com.binance.connector.futures.client.FuturesClient;
import com.binance.connector.futures.client.WebsocketClient;
import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.quant.observer.base.CandlestickObserver;
import com.digcoo.fitech.quant.observer.base.TradeObserver;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class DataFeedModule {
    private final HistoricalDataHandler historicalDataHandler;
    private final RealTimeDataHandler realTimeDataHandler;
    private final Config config;
    private final List<CandlestickObserver> candlestickObservers;
    private final List<TradeObserver> tradeObservers;

    public DataFeedModule(FuturesClient restClient
                            , WebsocketClient websocketClient
                            , List<CandlestickObserver> candlestickObservers
                            , List<TradeObserver> tradeObservers
                            , Config config) {
        this.historicalDataHandler = new HistoricalDataHandler(restClient);
        this.realTimeDataHandler = new RealTimeDataHandler(websocketClient);
        this.candlestickObservers = candlestickObservers;
        this.tradeObservers = tradeObservers;
        this.config = config;
    }

    @PostConstruct
    public void init() {
        addCandleObserver(candlestickObservers);
        addTradeObserver(tradeObservers);
    }

    public List<Candlestick> queryHistoryData(String symbol, CandlestickPeriod period) {
        List<Candlestick> historicalCandlesticks = this.historicalDataHandler.getCandles(symbol, period, config.getDataLimit());
        return historicalCandlesticks;
    }

    public void loadHistoryData(String symbol, CandlestickPeriod period, List<Candlestick> historicalCandlesticks) {
        realTimeDataHandler.initHistoricalCandlesticks(symbol, period, historicalCandlesticks);
    }

    public void addCandleObserver(List<CandlestickObserver> candlestickObservers) {
        for (CandlestickObserver candlestickObserver : candlestickObservers) {
            realTimeDataHandler.addCandleObserver(candlestickObserver);
        }
    }

    public void addTradeObserver(List<TradeObserver> tradeObservers) {
        for (TradeObserver tradeObserver : tradeObservers) {
            realTimeDataHandler.addTradeObserver(tradeObserver);
        }
    }

    public void updateRealtimeCandlestick(String symbol, CandlestickPeriod period, Candlestick realtimeCandlestick) {
        realTimeDataHandler.updateRealTimeKline(realtimeCandlestick);
        realTimeDataHandler.notifyCandleSubscribers(symbol, period, realtimeCandlestick);
    }

    //加载历史数据
    public List<Candlestick> loadHistoryData(String symbol, CandlestickPeriod period) {
        List<Candlestick> historicalCandlesticks = this.historicalDataHandler.getCandles(symbol, period, config.getDataLimit());
        realTimeDataHandler.initHistoricalCandlesticks(symbol, period, historicalCandlesticks);
        return historicalCandlesticks;
    }

    // 订阅实时数据
    public void subscribeMarketData(String symbol, CandlestickPeriod period) {
        realTimeDataHandler.subscribeCandlestick(symbol, period);
    }

    public List<Candlestick> getCandlesticks(String symbol, CandlestickPeriod period, int limit) {
        //1、先加载历史数据
//        this.realTimeData.initHistoricalCandlesticks();
        //2、再订阅实时数据
//        this.realTimeData.subscribeCandlestick();
        //3、获取实时数据
        return realTimeDataHandler.getCandlesticks(symbol, period, limit);
    }

    public List<String> getTopVolumeSymbols(int topK) {
        return this.historicalDataHandler.getTopVolumeSymbols(topK);
    }

}
