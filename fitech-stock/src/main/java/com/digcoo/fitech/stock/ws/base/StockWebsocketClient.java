package com.digcoo.fitech.stock.ws.base;

import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.ws.PriceUpdateCallback;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
public abstract class StockWebsocketClient {

    private final int refreshPeriod;
    private List<String> subscribeSymbols = new ArrayList<>();
    private final Map<String, List<PriceUpdateCallback>> subscriberMap = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();
    private ScheduledFuture<?> updateTask;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public StockWebsocketClient(int refreshPeriod) {
        this.refreshPeriod = refreshPeriod;
    }

    /**
     * 启动价格更新服务
     */
    public void start() {

        if (updateTask != null && !updateTask.isDone()) {
            return;
        }

        updateTask = scheduler.scheduleAtFixedRate(
                this::refreshData,
                0,
                refreshPeriod,
                TimeUnit.SECONDS
        );
    }

    /**
     * 停止价格更新服务
     */
    public void stop() {
        if (updateTask != null) {
            updateTask.cancel(false);
        }
        scheduler.shutdown();
    }

    /**
     * 订阅股票价格更新
     */
    public void subscribe(String symbol, List<PriceUpdateCallback> callbacks) {
        lock.lock();
        try {
            if (!this.subscribeSymbols.contains(symbol)){
                this.subscribeSymbols.add(symbol);
            }

            this.subscriberMap.putIfAbsent(symbol, new ArrayList<>());
            this.subscriberMap.get(symbol).addAll(callbacks);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 更新价格数据
     */
    private void refreshData() {
        try {
            if (CollectionUtils.isEmpty(this.subscriberMap)) {
                return;
            }

            long startTime = System.currentTimeMillis();
            //分批异步更新及处理
            Lists.partition(this.subscribeSymbols, 100).forEach(symbols ->
                this.executor.execute(() -> {
                try {
                    List<Candlestick> candlesticks = refreshCandlestick(symbols);
                    if (!CollectionUtils.isEmpty(candlesticks)) {
                        for (Candlestick realtimeCandlestick : candlesticks) {

//                            lock.lock();
                            try {
                                // 通知订阅者
                                for (PriceUpdateCallback callback : this.subscriberMap.get(realtimeCandlestick.getSymbol())) {
                                    try {
                                        callback.onReceive(realtimeCandlestick);
                                    } catch (Exception e) {
                                        log.error("error subscriber callback...", e);
                                    }
                                }
                            } finally {
//                                lock.unlock();
                            }

                        }
                    }
                }catch (Exception ex) {
                    log.error("error refreshData...", ex);
                }
            }));

            log.info("refreshData cost time: {} ms", System.currentTimeMillis() - startTime);

        } catch (Exception ex) {
            log.error("error refreshData...", ex);
        }
    }

    protected abstract List<Candlestick> refreshCandlestick(List<String> symbols);

}
