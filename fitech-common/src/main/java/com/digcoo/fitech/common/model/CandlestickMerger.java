package com.digcoo.fitech.common.model;


import com.digcoo.fitech.common.enums.CandlestickPeriod;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

@Data
public class CandlestickMerger {
    private final ConcurrentSkipListMap<Long, Candlestick> candlestickMergeMap = new ConcurrentSkipListMap<>();
    private final String symbol;
    private final CandlestickPeriod period;

    public CandlestickMerger(String symbol, CandlestickPeriod period) {
        this.symbol = symbol;
        this.period = period;
    }

    /**
     * 初始化历史数据
     */
    public synchronized void initHistoricalData(List<Candlestick> historicalCandlesticks) {
        candlestickMergeMap.clear();
        historicalCandlesticks.forEach(k -> candlestickMergeMap.put(k.getOpenTime(), k));
    }

    /**
     * 更新实时K线数据
     */
    public synchronized void updateRealTimeKline(Candlestick realtimeKline) {
        long openTime = realtimeKline.getOpenTime();

        if (candlestickMergeMap.containsKey(openTime)) {
            // 更新当前K线
            Candlestick existing = candlestickMergeMap.get(openTime);
            updateExistingKline(existing, realtimeKline);
        } else {
            // 添加新K线
            candlestickMergeMap.put(openTime, realtimeKline);

            // 维护数据量，保留最近N根K线
            if (candlestickMergeMap.size() > 1500) {
                candlestickMergeMap.pollFirstEntry();
            }
        }
    }

    /**
     * 更新已有K线
     */
    private void updateExistingKline(Candlestick existing, Candlestick update) {
        existing.setHigh(update.getHigh().max(existing.getHigh()));
        existing.setLow(update.getLow().min(existing.getLow()));
        existing.setClose(update.getClose());
        existing.setVolume(existing.getVolume().add(update.getVolume()));
        existing.setNumberOfTrades(existing.getNumberOfTrades() + update.getNumberOfTrades());

        // 如果实时数据标记K线闭合，则更新状态
        if (update.isClosed()) {
            existing.setClosed(true);
        }
    }

    /**
     * 获取合并后的K线列表
     */
    public synchronized List<Candlestick> getCandlesticks() {
        return new ArrayList<>(candlestickMergeMap.values());
    }

    /**
     * 获取指定数量的最新K线
     */
    public synchronized List<Candlestick> getCandlesticks(int limit) {
        return candlestickMergeMap.descendingMap().values().stream()
                .limit(limit)
                .sorted(Comparator.comparingLong(Candlestick::getOpenTime))
                .toList();
    }

}
