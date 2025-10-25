package com.digcoo.fitech.stock.core;


import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.util.http.SinaHttpClient;
import com.digcoo.fitech.common.util.http.XueQiuHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class HistoricalDataHandler {

    private SinaHttpClient sinaHttpClient;
    private XueQiuHttpClient xueQiuHttpClient;
    private List<String> watchSymbols;

    public HistoricalDataHandler(SinaHttpClient sinaHttpClient, XueQiuHttpClient xueQiuHttpClient) {
        this.sinaHttpClient = sinaHttpClient;
        this.xueQiuHttpClient = xueQiuHttpClient;
    }

    /**
     * 获取K线历史数据（雪球）
     * @param symbol
     * @param period K线间隔，如1m, 5m, 1h, 1d等
     * @param limit 获取数量限制
     * @return 蜡烛图数据列表
     */
    public List<Candlestick> getCandles(String symbol, CandlestickPeriod period, int limit) {
        try {
            return xueQiuHttpClient.getHistoryCandles(symbol, period, limit);
        } catch (Exception ex) {
            log.error("Failed to fetch historical data...", ex);
            return Collections.emptyList();
        }
    }

    public List<String> getWatchSymbols(int topK) {
        if (CollectionUtils.isEmpty(watchSymbols)) {
            List<Candlestick> hsA = sinaHttpClient.requestAllSymbols("sh_a");
            List<Candlestick> szA = sinaHttpClient.requestAllSymbols("sz_a");
            this.watchSymbols = Stream.concat(hsA.stream(), szA.stream())
                    .map(Candlestick::getSymbol)
                    .collect(Collectors.toList());
        }
        return watchSymbols;
    }

}
