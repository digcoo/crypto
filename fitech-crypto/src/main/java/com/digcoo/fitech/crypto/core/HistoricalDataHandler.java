package com.digcoo.fitech.crypto.core;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.binance.connector.futures.client.FuturesClient;
import com.binance.connector.futures.client.exceptions.BinanceClientException;
import com.binance.connector.futures.client.exceptions.BinanceConnectorException;
import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.model.OrderBook;
import com.digcoo.fitech.common.model.ticker.Ticker24H;
import com.digcoo.fitech.common.model.market.ExchangeInformation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class HistoricalDataHandler {
    FuturesClient restClient;

    public HistoricalDataHandler(FuturesClient restClient) {
        this.restClient = restClient;
    }

    /**
     * 获取K线历史数据
     * @param symbol 交易对，如BTCUSDT
     * @param period K线间隔，如1m, 5m, 1h, 1d等
     * @param limit 获取数量限制
     * @return 蜡烛图数据列表
     */
    public List<Candlestick> getCandles(String symbol, CandlestickPeriod period, int limit) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("interval", period.getCoinPeriod());
        parameters.put("limit", limit);
//        parameters.put("startTime", startTime);
//        parameters.put(("endTime", endTime);

        try {
            String response = restClient.market().klines(parameters);
            return parseCandlestickData(symbol, period, response);
        } catch (BinanceConnectorException | BinanceClientException e) {
            log.error("Failed to fetch historical data: " + e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析币安返回的K线数据
     */
    private List<Candlestick> parseCandlestickData(String symbol, CandlestickPeriod period, String jsonResponse) {
        JSONArray jsonArray = JSON.parseArray(jsonResponse);
        List<Candlestick> candles = new ArrayList<>(jsonArray.size());

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray dataArray = jsonArray.getJSONArray(i);
            Candlestick candle = new Candlestick();
            candle.setOpenTime(dataArray.getLong(0));
            candle.setOpen(dataArray.getBigDecimal(1));
            candle.setHigh(dataArray.getBigDecimal(2));
            candle.setLow(dataArray.getBigDecimal(3));
            candle.setClose(dataArray.getBigDecimal(4));
            candle.setVolume(dataArray.getBigDecimal(5));
            candle.setCloseTime(dataArray.getLong(6));
            candle.setAmount(dataArray.getBigDecimal(7));
            candle.setNumberOfTrades(dataArray.getInteger(8));
            candle.setTakerBuyBaseAssetVolume(dataArray.getBigDecimal(9));
            candle.setTakerBuyQuoteAssetVolume(dataArray.getBigDecimal(10));
            candle.setSymbol(symbol);
            candle.setPeriod(period);

            candles.add(candle);
        }

        return candles;
    }

    /**
     * 获取24小时价格变动数据
     */
    public Ticker24H get24HTicker(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);

        try {
            String response = restClient.market().ticker24H(parameters);
            return JSON.parseObject(response, Ticker24H.class);
        } catch (BinanceConnectorException | BinanceClientException e) {
            log.error("Failed to fetch 24h ticker: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取当前订单簿
     */
    public OrderBook getOrderBook(String symbol, int limit) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("limit", limit);

        try {
            String response = restClient.market().depth(parameters);
            return JSON.parseObject(response, OrderBook.class);
        } catch (BinanceConnectorException | BinanceClientException e) {
            log.error("Failed to fetch order book: {}", e.getMessage());
            return null;
        }
    }

    public List<String> getTopVolumeSymbols(int topK) {
        ExchangeInformation exchangeInformation = JSON.parseObject(this.restClient.market().exchangeInfo(), ExchangeInformation.class);
        Set<String> symbols = exchangeInformation.getSymbols().stream().filter(x -> x.getSymbol().endsWith("USDT")).map(x -> x.getSymbol()).collect(Collectors.toSet());

        Map<String, BigDecimal> symbolToQuoteAssetVolumeMap = new HashMap<>();

        for (String symbol : symbols) {
            try {
                List<Candlestick> dayCandlesticks = getCandles(symbol, CandlestickPeriod.DAILY, 5);
                Candlestick dayCandlestick1 = dayCandlesticks.get(dayCandlesticks.size() - 2);
//                if (dayCandlestick1.getQuoteAssetVolume().compareTo(new BigDecimal(1_0000_0000 * 1)) > 0) {
                    symbolToQuoteAssetVolumeMap.put(symbol, dayCandlestick1.getAmount());
//                }
            }catch (Exception ex) {
                log.error("getTopVolumeSymbols error. symbol = {}", symbol, ex);
            }
        }

        List<Map.Entry<String, BigDecimal>> sortedList = new ArrayList<>(symbolToQuoteAssetVolumeMap.entrySet());
        sortedList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        List<Map.Entry<String, BigDecimal>> topEntries = sortedList.size() > topK ? sortedList.subList(0, topK) : sortedList;

        return topEntries.stream().map(x -> x.getKey()).collect(Collectors.toList());

    }

}
