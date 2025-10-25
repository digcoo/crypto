package com.digcoo.fitech.stock.core;

import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.common.constants.GlobalConstants;
import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.stock.observer.base.CandlestickObserver;
import com.digcoo.fitech.stock.observer.base.TradeObserver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Component
@Slf4j
public class DataFeedModule {
    private final HistoricalDataHandler historicalDataHandler;
    private final RealTimeDataHandler realTimeDataHandler;
    private final Config config;
    private final List<CandlestickObserver> candlestickObservers;
    private final List<TradeObserver> tradeObservers;

    public DataFeedModule(HistoricalDataHandler historicalDataHandler
                            , RealTimeDataHandler realTimeDataHandler
                            , List<CandlestickObserver> candlestickObservers
                            , List<TradeObserver> tradeObservers
                            , Config config) {
        this.historicalDataHandler = historicalDataHandler;
        this.realTimeDataHandler = realTimeDataHandler;
        this.candlestickObservers = candlestickObservers;
        this.tradeObservers = tradeObservers;
        this.config = config;
    }

    @PostConstruct
    public void init() {
        addCandleObserver(candlestickObservers);
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

    public List<String> getWatchSymbols(int topK) {
        return this.historicalDataHandler.getWatchSymbols(topK);
    }

    public void warmUp() {

        try {

            log.info("warmup start");

            if (System.getenv(GlobalConstants.WARM_UP_MODE_KEY).equals(GlobalConstants.WARM_UP_SNAPSHOT)) {

                log.info("warmup restoreSnapshot history market candlesticks..");

                this.realTimeDataHandler.restoreSnapshot();

                List<String> allSymbols = this.realTimeDataHandler.getAllSymbols();

                for (String symbol : allSymbols) {
                    // 订阅实时数据
                    subscribeMarketData(symbol, config.getPeriod());
                }

            } else {

                log.info("warmup init history market candlesticks..");

                List<String> allSymbols = getWatchSymbols(config.getTopKSymbolCount());

                if (CollectionUtils.isEmpty(allSymbols)) {
                    log.error("no symbol found, system exit...");
                    System.exit(-1);
                }

                for (String symbol : allSymbols) {
                    //初始化历史数据
                    loadHistoryData(symbol, config.getPeriod());

                    // 订阅实时数据
                    subscribeMarketData(symbol, config.getPeriod());

//                    break;

                }

                log.info("warmup: snapshot history market candlesticks..");

                // 备份
                this.realTimeDataHandler.saveSnapshot();
            }

            log.info("warmup finish");

        }catch (Exception ex) {
            log.error("warmup error", ex);
        }

    }

    public void start() {
        try {

            log.info("realtime strategy start...");

            this.realTimeDataHandler.start();

        } catch (Exception ex) {
            log.error("strategy start error", ex);
        }
    }

}
