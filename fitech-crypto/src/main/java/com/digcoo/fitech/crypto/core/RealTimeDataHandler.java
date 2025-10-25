package com.digcoo.fitech.crypto.core;

import com.binance.connector.futures.client.WebsocketClient;
import com.binance.connector.futures.client.utils.WebSocketCallback;
import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.model.CandlestickMerger;
import com.digcoo.fitech.common.model.ticker.Ticker24H;
import com.digcoo.fitech.common.model.Trade;
import com.digcoo.fitech.crypto.core.callback.CandlestickWSCallback;
import com.digcoo.fitech.crypto.core.callback.TradeWSCallback;
import com.digcoo.fitech.crypto.observer.base.CandlestickObserver;
import com.digcoo.fitech.crypto.observer.base.TradeObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class RealTimeDataHandler {
    private final WebsocketClient wsClient;
    private final Map<String, Integer> subscriptionCounts = new ConcurrentHashMap<>();
    private final Map<String, Candlestick> latestCandles = new ConcurrentHashMap<>();
    private final Map<String, CandlestickMerger> klineMergers = new ConcurrentHashMap<>();

    private final Map<String, Ticker24H> latestTickers = new ConcurrentHashMap<>();

//    // 观察者集合
//    private final Map<String, Set<CandlestickObserver>> candleObservers = new ConcurrentHashMap<>();
//    private final Map<String, Set<TradeObserver>> tradeObservers = new ConcurrentHashMap<>();
    private final Set<CandlestickObserver> candleObservers = ConcurrentHashMap.newKeySet();
    private final Set<TradeObserver> tradeObservers = ConcurrentHashMap.newKeySet();


    private final ExecutorService notificationExecutor = Executors.newCachedThreadPool();

    public RealTimeDataHandler(WebsocketClient websocketClient) {
        this.wsClient = websocketClient;
    }



    public void addCandleObserver(CandlestickObserver observer) {
        candleObservers.add(observer);
    }

    public void addTradeObserver(TradeObserver observer) {
        tradeObservers.add(observer);
    }

//    public void addCandleObserver(String symbol, CandlestickPeriod period
//                                  , CandlestickObserver observer) {
//        String key = getCandleKey(symbol, period);
//        candleObservers.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(observer);
//    }
//    public void addCandleObserver(String symbol, CandlestickPeriod period
//                                , Predicate<List<Candlestick>> filter
//                                , CandlestickObserver observer) {
//        String key = getCandleKey(symbol, period);
//        candleObservers.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
//                .add((s, i, candlestick) -> {
//                    if (filter.test(candlestick)) {
//                        observer.onCandleUpdate(s, i, candlestick);
//                    }
//                });
//    }
//
//    public void addTradeObserver(String symbol, TradeObserver observer) {
//        tradeObservers.computeIfAbsent(symbol, k -> ConcurrentHashMap.newKeySet()).add(observer);
//    }
//
//    public void addTradeObserver(String symbol
//                                , Predicate<Trade> filter
//                                , TradeObserver observer) {
//        tradeObservers.computeIfAbsent(symbol, k -> ConcurrentHashMap.newKeySet())
//                .add((s, trade) -> {
//                    if (filter.test(trade)) {
//                        observer.onTradeUpdate(s, trade);
//                    }
//                });
//    }



    /**
     * 订阅逐笔交易数据
     */
    public void subscribeTrade(String symbol) {
        String streamName = String.format("%s", symbol.toLowerCase());
        subscribeTrade(streamName, new TradeWSCallback(symbol, this));
    }

    /**
     * 订阅K线数据流
     * @param symbol 交易对，如BTCUSDT
     * @param period K线间隔，如1m, 5m, 1h等
     */
    public void subscribeCandlestick(String symbol, CandlestickPeriod period) {
        String key = getCandleKey(symbol, period);
        klineMergers.computeIfAbsent(key, k -> new CandlestickMerger(symbol, period));
        subscribeCandlestick(symbol, period, new CandlestickWSCallback(symbol, period, this));
    }

    /**
     * 订阅每笔交易
     */
    private void subscribeTrade(String symbol, WebSocketCallback callback) {
        subscriptionCounts.merge(symbol, 1, Integer::sum);
        if (subscriptionCounts.get(symbol) == 1) {
            wsClient.aggTradeStream(symbol, callback);
        }
    }

    /**
     * 通用订阅方法
     */
    private void subscribeCandlestick(String symbol, CandlestickPeriod period, WebSocketCallback callback) {
        subscriptionCounts.merge(symbol, 1, Integer::sum);
        if (subscriptionCounts.get(symbol) == 1) {
            wsClient.klineStream(symbol, period.getCoinPeriod(), callback);
        }
    }


    /**
     * 取消订阅
     */
    public void unsubscribe(String streamName) {
        if (subscriptionCounts.containsKey(streamName)) {
            int count = subscriptionCounts.merge(streamName, -1, Integer::sum);
            if (count <= 0) {
                wsClient.closeAllConnections();
                subscriptionCounts.remove(streamName);
            }
        }
    }

//    /**
//     * 移除观察者
//     * @param symbol
//     * @param period
//     * @param observer
//     */
//    public void removeCandleObserver(String symbol, CandlestickPeriod period, CandlestickObserver observer) {
//        String key = getCandleKey(symbol, period);
//        if (candleObservers.containsKey(key)) {
//            candleObservers.get(key).remove(observer);
//        }
//    }

    public void removeCandleObserver(CandlestickObserver observer) {
        candleObservers.remove(observer);
    }

    public void removeTradeObserver(TradeObserver observer) {
        tradeObservers.remove(observer);
    }

    /**
     * 获取最新的K线数据
     */
    public Candlestick getLatestCandle(String symbol, CandlestickPeriod period) {
        return this.latestCandles.get(getCandleKey(symbol, period));
    }

    /**
     * 更新实时K线数据
     */
    public synchronized void updateRealTimeKline(Candlestick realtimeCandlestick) {
        if (Objects.nonNull(realtimeCandlestick)) {
            String candleKey = getCandleKey(realtimeCandlestick.getSymbol(), realtimeCandlestick.getPeriod());
            CandlestickMerger candlestickMerger = klineMergers.get(candleKey);
            if (candlestickMerger != null) {
                candlestickMerger.updateRealTimeKline(realtimeCandlestick);
            }
        }
    }

    /**
     * 更新实时K线数据
     */
    public synchronized void updateRealTimeTrade(Trade realtimeTrade) {
        if (Objects.nonNull(realtimeTrade)) {
            String symbol = realtimeTrade.getSymbol();
            if (latestTickers.containsKey(symbol)) {
                latestTickers.get(symbol).setLastPrice(realtimeTrade.getPrice());
            }
        }
    }

    public void notifyCandleSubscribers(String symbol, CandlestickPeriod period, Candlestick candlestick) {
        String candleKey = getCandleKey(symbol, period);
        List<Candlestick> candlesticks = klineMergers.get(candleKey).getCandlesticks(100);
        if (candleObservers != null) {
            candleObservers.forEach(observer -> {
                notificationExecutor.submit(() -> {
                    try {
                        observer.onCandleUpdate(symbol, period, candlesticks);
                    } catch (Exception e) {
                        log.error("Error notifying candle observer: " + e.getMessage(), e);
                    }
                });
            });
        }
    }

    public void notifyTradeSubscribers(String symbol, Trade trade) {
        if (tradeObservers != null) {
            tradeObservers.forEach(observer -> {
                notificationExecutor.submit(() -> {
                    try {
                        observer.onTradeUpdate(symbol, trade);
                    } catch (Exception e) {
                        log.error("Error notifying trade observer: " + e.getMessage(), e);
                    }
                });
            });
        }
    }

    public void initHistoricalCandlesticks(String symbol, CandlestickPeriod period, List<Candlestick> historicalCandlesticks) {
        String candleKey = getCandleKey(symbol, period);
        CandlestickMerger candlestickMerger = klineMergers.computeIfAbsent(candleKey, k -> new CandlestickMerger(symbol, period));
        candlestickMerger.initHistoricalData(historicalCandlesticks);
    }

    public List<Candlestick> getCandlesticks(String symbol, CandlestickPeriod period) {
        String candleKey = getCandleKey(symbol, period);
        CandlestickMerger candlestickMerger = klineMergers.get(candleKey);
        return candlestickMerger != null ? candlestickMerger.getCandlesticks() : Collections.emptyList();
    }

    public List<Candlestick> getCandlesticks(String symbol, CandlestickPeriod period, int limit) {
        String candleKey = getCandleKey(symbol, period);
        CandlestickMerger candlestickMerger = klineMergers.get(candleKey);
        return candlestickMerger != null ? candlestickMerger.getCandlesticks(limit) : Collections.emptyList();
    }

    private String getCandleKey(String symbol, CandlestickPeriod period) {
        return symbol + ":" + period;
    }

}
