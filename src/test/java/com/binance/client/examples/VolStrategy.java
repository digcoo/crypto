package com.binance.client.examples;


import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ExchangeInformation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 顺势策略
 *
 */
@Slf4j
public class VolStrategy {

    public static final String API_KEY = "fAgJpcu289fMzPEP1YoeTz0Q4zOhly40d2vHoufpYu6oqiYxM2iUkCBeM0b2iVvG";
    public static final String SECRET_KEY = "IQcZ7D1N0l44cP61Xcw2LxTlSIK4zSV5uSgtKjKZZuleNfIXbvuJEGzAoGTMzeCQ"; // Unnecessary if PRIVATE_KEY_PATH is used
    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";

    public static void main(String[] args) {
        int limit = 12;  //取5条k数据
//        BigDecimal zhenfuRate = new BigDecimal(0.5);  //振幅
//        BigDecimal tradeAssetVolume = new BigDecimal(1_000_000);  //交易量
////        CandlestickInterval period = CandlestickInterval.HOURLY;     //周期

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

                List<Candlestick> longList = new ArrayList<>();
                List<Candlestick> shortList = new ArrayList<>();

                /**
                 * 基本策略：
                 * 1、出现梯子
                 * 2、顺势而为
                 */
                for (String symbol : symbols) {
                    try {
//                        List<SymbolOrderBook> symbolOrderBookTicker = syncRequestClient.getSymbolOrderBookTicker(symbol);
//                        List<Candlestick> monthCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MONTH.getInterval(), null, null, limit);
//                        List<Candlestick> weekCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.WEEK.getInterval(), null, null, limit);
//                        List<Candlestick> dayCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.DAY.getInterval(), null, null, limit);
                        List<Candlestick> hour4Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.HOUR4.getInterval(), null, null, limit);
//                        List<Candlestick> hour1Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.HOUR1.getInterval(), null, null, limit);
//                        List<Candlestick> min30Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MIN30.getInterval(), null, null, limit);

                        if (symbol.equals("FILUSDT")) {
                            System.out.println("======");
                        }

                        //1h=5_000U(1秒) * 60（1分钟） * 60（1小时）=18_00_0000 ~ 2000_0000
                        //4h= 4 * 18_00_0000 =  72_00_0000 ~ 8000_0000
                        //1d= 6 * 4 * 18_00_0000 =  6 ^ 8000_0000 = 4_0000_0000

                        //取出candlesticks中最大成交量的记录
                        Candlestick maxVolCandlestick = hour4Candlesticks.stream().max(Comparator.comparing(Candlestick::getVolume)).orElse(null);
                        Candlestick lastCandlestick1 = hour4Candlesticks.get(hour4Candlesticks.size() - 1);
                        Candlestick lastCandlestick2 = hour4Candlesticks.get(hour4Candlesticks.size() - 2);
                        Candlestick lastCandlestick3 = hour4Candlesticks.get(hour4Candlesticks.size() - 2);


                        BigDecimal jumpRateThreshold = new BigDecimal(0.01);
                        BigDecimal usdtAmountThreshold = new BigDecimal(8000_0000);

                        // 成交额
                        if (lastCandlestick1.getQuoteAssetVolume().compareTo(usdtAmountThreshold) < 0
                                && lastCandlestick2.getQuoteAssetVolume().compareTo(usdtAmountThreshold) < 0
                                && lastCandlestick3.getQuoteAssetVolume().compareTo(usdtAmountThreshold) < 0) {
                            continue;
                        }

                        //振幅
                        if (lastCandlestick1.getZhenFuRate().compareTo(jumpRateThreshold) < 0
                                && lastCandlestick2.getZhenFuRate().compareTo(jumpRateThreshold) < 0
                                && lastCandlestick3.getZhenFuRate().compareTo(jumpRateThreshold) < 0) {
                            continue;
                        }

                        //成交量放大（趋势顺势或逆转）
                        if (maxVolCandlestick.getShiTiRate().compareTo(BigDecimal.ZERO) > 0) {
//                            if (lastCandlestick1.getClose().compareTo(maxVolCandlestick.getShiTiMax()) > 0
                            if (lastCandlestick1.getClose().compareTo(maxVolCandlestick.getShiTiMin()) > 0
//                                    同向
                                    && lastCandlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                            ) {
                                longList.add(lastCandlestick1);
                            }else if (lastCandlestick1.getClose().compareTo(maxVolCandlestick.getShiTiMin()) < 0
//                                    同向
                                    && lastCandlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                            ) {
                                shortList.add(lastCandlestick1);
                            }
                        } else if (maxVolCandlestick.getShiTiRate().compareTo(BigDecimal.ZERO) < 0) {
//                            if (lastCandlestick1.getClose().compareTo(maxVolCandlestick.getShiTiMin()) < 0
                            if (lastCandlestick1.getClose().compareTo(maxVolCandlestick.getShiTiMax()) < 0
                                    //同向
                                    && lastCandlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                            ) {
                                shortList.add(lastCandlestick1);
                            }else if (lastCandlestick1.getClose().compareTo(maxVolCandlestick.getShiTiMax()) > 0
                                    //同向
                                    && lastCandlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                            ) {
                                longList.add(lastCandlestick1);
                            }
                        }

                    } catch (Exception ex) {
                        log.error("symbol = {}", symbol, ex);
                    }
                }

                longList.sort(Comparator.comparing(Candlestick::getZhenFuRate).reversed());
                shortList.sort(Comparator.comparing(Candlestick::getZhenFuRate).reversed());

                stopWatch.stop();

                for (Candlestick longCandlestick : longList) {
                    log.warn("多头命中: {}, {}", link_pref + longCandlestick.getSymbol(),
                            longCandlestick.getQuoteAssetVolume());
                }
                for (Candlestick shortCandlestick : shortList) {
                    log.error("空头命中: {}, {}", link_pref + shortCandlestick.getSymbol(),
                            shortCandlestick.getQuoteAssetVolume());
                }

                log.warn("多头命中[{}/{}]", longList.size(), symbols.size());
                log.error("空头命中[{}/{}]", shortList.size(), symbols.size());

                log.info("总耗时[{}]秒", stopWatch.getTime() / 1000);



            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0, 5, TimeUnit.MINUTES);
    }
}
