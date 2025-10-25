package com.digcoo.fitech.stock.core;

import com.alibaba.fastjson2.JSON;
import com.digcoo.fitech.common.constants.GlobalConstants;
import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.model.CandlestickMerger;
import com.digcoo.fitech.common.model.CandlestickSnapshot;
import com.digcoo.fitech.common.util.FileUtil;
import com.digcoo.fitech.common.ws.PriceUpdateCallback;
import com.digcoo.fitech.stock.core.callback.CandlestickWSCallback;
import com.digcoo.fitech.stock.observer.base.CandlestickObserver;
import com.digcoo.fitech.stock.ws.base.StockWebsocketClient;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RealTimeDataHandler {

    private final StockWebsocketClient wsClient;
    private final Map<String, Integer> subscriptionCounts = new ConcurrentHashMap<>();
    private final Map<String, Candlestick> latestCandles = new ConcurrentHashMap<>();
    private final Map<String, CandlestickMerger> klineMergers = new ConcurrentHashMap<>();
    private final List<String> allSymbols = new ArrayList<>();

    //symbol -> period -> CandlestickMerger
//    private final Map<String, Map<CandlestickPeriod, CandlestickMerger>> symbolToPeriodToKlineMergerMap = new ConcurrentHashMap<>();

    private final Set<CandlestickObserver> candleObservers = ConcurrentHashMap.newKeySet();
//    private final Set<TradeObserver> tradeObservers = ConcurrentHashMap.newKeySet();

    //TODO 分析当前价格在24Ticker线的位置

//    static ObjectMapper objectMapper = new ObjectMapper();
//    static {
//        StreamReadConstraints constraints = StreamReadConstraints.builder()
//                .maxStringLength(50_000_000) // 50MB
//                .build();
//        objectMapper.getFactory().setStreamReadConstraints(constraints);
//    }


    private final ExecutorService notificationExecutor = Executors.newCachedThreadPool();

    public RealTimeDataHandler(StockWebsocketClient websocketClient) {
        this.wsClient = websocketClient;
    }

    public void addCandleObserver(CandlestickObserver observer) {
        candleObservers.add(observer);
    }

    /**
     * 订阅K线数据流
     * @param symbol 交易对，如BTCUSDT
     * @param period K线间隔，如1m, 5m, 1h等
     */
    public void subscribeCandlestick(String symbol, CandlestickPeriod period) {
        String key = getCandleKey(symbol, period);
        klineMergers.computeIfAbsent(key, k -> new CandlestickMerger(symbol, period));

        List<PriceUpdateCallback> callbacks = Arrays.asList(
                new CandlestickWSCallback(symbol, period, this));
        subscribeCandlestick(symbol, callbacks);
    }

    /**
     * 通用订阅方法
     */
    private void subscribeCandlestick(String symbol, List<PriceUpdateCallback> callbacks) {
        this.wsClient.subscribe(symbol, callbacks);
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

    public void notifyCandleSubscribers(String symbol, CandlestickPeriod period, Candlestick candlestick) {
        String candleKey = getCandleKey(symbol, period);
        List<Candlestick> candlesticks = this.klineMergers.get(candleKey).getCandlesticks(100);
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

    public void initHistoricalCandlesticks(String symbol, CandlestickPeriod period, List<Candlestick> historicalCandlesticks) {
        String candleKey = getCandleKey(symbol, period);
        CandlestickMerger candlestickMerger = klineMergers.computeIfAbsent(candleKey, k -> new CandlestickMerger(symbol, period));
        candlestickMerger.initHistoricalData(historicalCandlesticks);
    }


    public void saveSnapshot() throws IOException {
        List<CandlestickSnapshot> snapshotData = new ArrayList<>();
        for (Map.Entry<String, CandlestickMerger> entry: this.klineMergers.entrySet()) {
            CandlestickMerger merger = entry.getValue();
            String symbol = merger.getSymbol();
            CandlestickPeriod period = merger.getPeriod();
            List<Candlestick> candlesticks = merger.getCandlesticks();

            CandlestickSnapshot candlestickSnapshot = new CandlestickSnapshot();
            candlestickSnapshot.setSymbol(symbol);
            candlestickSnapshot.getCandlesticksMap().put(period.getStockPeriod(), candlesticks);
            snapshotData.add(candlestickSnapshot);

        }

        //保存到本地文件
        FileUtil.saveSnapshotGz(snapshotData);
    }

    public void restoreSnapshot() throws IOException {
        List<CandlestickSnapshot> snapshotData = FileUtil.loadSnapshotGz();
        for (CandlestickSnapshot snapshot : snapshotData) {
            String symbol = snapshot.getSymbol();
            for (Map.Entry<String, List<Candlestick>> entry : snapshot.getCandlesticksMap().entrySet()) {
                String period = entry.getKey();
                List<Candlestick> candlesticks = entry.getValue();

                //初始化K线数据
                initHistoricalCandlesticks(symbol, CandlestickPeriod.toByStockPeriod(period), candlesticks);
            }

        }

    }

    public List<Candlestick> getCandlesticks(String symbol, CandlestickPeriod period) {
        String candleKey = getCandleKey(symbol, period);
        CandlestickMerger candlestickMerger = this.klineMergers.get(candleKey);
        return candlestickMerger != null ? candlestickMerger.getCandlesticks() : Collections.emptyList();
    }

    public List<Candlestick> getCandlesticks(String symbol, CandlestickPeriod period, int limit) {
        String candleKey = getCandleKey(symbol, period);
        CandlestickMerger candlestickMerger = this.klineMergers.get(candleKey);
        return candlestickMerger != null ? candlestickMerger.getCandlesticks(limit) : Collections.emptyList();
    }

    private String getCandleKey(String symbol, CandlestickPeriod period) {
        return symbol + ":" + period.getStockPeriod();
    }

    public List<String> getAllSymbols() {
        return this.klineMergers.values().stream().map(CandlestickMerger::getSymbol).collect(Collectors.toList());
    }

    public void start() {
        this.wsClient.start();
    }
}
