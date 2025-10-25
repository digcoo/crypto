package com.binance.client.examples;


import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.dto.LongCandlestickDTO;
import com.binance.client.dto.LongTizi;
import com.binance.client.dto.LongTiziCalculate;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ExchangeInformation;
import com.binance.client.model.market.LongCandlestickMA;
import com.binance.client.model.market.SymbolOrderBook;
import com.binance.client.utils.LongStrategyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 突破梯子做多策略
 *
 */
@Slf4j
public class ZhangFuStrategy {

    public static final String API_KEY = "fAgJpcu289fMzPEP1YoeTz0Q4zOhly40d2vHoufpYu6oqiYxM2iUkCBeM0b2iVvG";
    public static final String SECRET_KEY = "IQcZ7D1N0l44cP61Xcw2LxTlSIK4zSV5uSgtKjKZZuleNfIXbvuJEGzAoGTMzeCQ"; // Unnecessary if PRIVATE_KEY_PATH is used
    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";

    public static void main(String[] args) {
        int limit = 1;  //取5条k数据
        BigDecimal zhenfuRate = new BigDecimal(0.5);  //振幅
        BigDecimal tradeAssetVolume = new BigDecimal(1_000_000);  //交易量
//        CandlestickInterval period = CandlestickInterval.HOURLY;     //周期

        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);

        //获取所有合约交易对
        ExchangeInformation exchangeInformation = syncRequestClient.getExchangeInformation();
        List<String> symbols = exchangeInformation.getSymbols().stream().filter(x -> x.getSymbol().endsWith("USDT")).map(x -> x.getSymbol()).collect(Collectors.toList());
//        symbols.stream().forEach(x -> { System.out.println(x); });

        //定时拉取K线并判断
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {


                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                List<Candlestick> targetList = new ArrayList<>();

                /**
                 * 基本策略：
                 * 1、出现梯子
                 * 2、顺势而为
                 */
                for (String symbol : symbols) {
                    try {
                        List<SymbolOrderBook> symbolOrderBookTicker = syncRequestClient.getSymbolOrderBookTicker(symbol);
                        List<Candlestick> hour4Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.HOUR4.getInterval(), null, null, limit);
                        if (
                                hour4Candlesticks.stream().anyMatch(x -> x.getShiTiRate().abs().compareTo(new BigDecimal(0.02)) > 0)
                        ) {
                            log.info("命中: {}", link_pref + symbol);
                            targetList.add(hour4Candlesticks.get(hour4Candlesticks.size() - 1));
                        }
                    } catch (Exception ex) {
                        log.error("symbol = {}", symbol, ex);
                    }
                }
                targetList.sort(Comparator.comparing(Candlestick::getQuoteAssetVolume).reversed());

                stopWatch.stop();
                log.info("耗时[{}]秒，命中[{}]", stopWatch.getTime() / 1000, targetList.size());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0, 5, TimeUnit.MINUTES);
    }


}
