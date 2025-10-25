package com.binance.client.examples;


import com.alibaba.fastjson.JSON;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ExchangeInformation;
import com.binance.client.model.market.SymbolOrderBook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class ShunShiStrategy {

    public static final String API_KEY = "fAgJpcu289fMzPEP1YoeTz0Q4zOhly40d2vHoufpYu6oqiYxM2iUkCBeM0b2iVvG";
    public static final String SECRET_KEY = "IQcZ7D1N0l44cP61Xcw2LxTlSIK4zSV5uSgtKjKZZuleNfIXbvuJEGzAoGTMzeCQ"; // Unnecessary if PRIVATE_KEY_PATH is used
    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";

    public static void main(String[] args) {
        int limit = 3;  //取5条k数据
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
                        List<Candlestick> monthCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MONTH.getInterval(), null, null, limit);
                        List<Candlestick> weekCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.WEEK.getInterval(), null, null, limit);
                        List<Candlestick> dayCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.DAY.getInterval(), null, null, limit);
                        List<Candlestick> hour4Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.HOUR4.getInterval(), null, null, limit);
                        List<Candlestick> hour1Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.HOUR1.getInterval(), null, null, limit);
                        List<Candlestick> min15Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MIN15.getInterval(), null, null, limit);

                        if (symbol.equals("OPUSDT")) {
                            System.out.println("======");
                        }

                        //1h=5_000U(1秒) * 60（1分钟） * 60（1小时）=18_00_0000 ~ 2000_0000
                        //4h= 4 * 18_00_0000 =  72_00_0000 ~ 8000_0000
                        //1d= 6 * 4 * 18_00_0000 =  6 ^ 8000_0000 = 4_0000_0000


                        //多军
                        if (1==1
//                                && isShunShiLong(monthCandlesticks, new BigDecimal(0.1), new BigDecimal(0.1), new BigDecimal(120_0000_0000))
//                                && isShunShiLong(weekCandlesticks, new BigDecimal(0.1), new BigDecimal(12_0000_0000), false, true)
                                && isShunShiLong(dayCandlesticks, new BigDecimal(0.03), new BigDecimal(3_0000_0000), false, true)
                                && isShunShiLong(hour4Candlesticks, new BigDecimal(0.02), new BigDecimal(6000_0000), false, true)
                                && isShunShiLong(hour1Candlesticks, new BigDecimal(0.015), new BigDecimal(1500_0000), true, false)
//                                && isShunShiLong(min30Candlesticks, new BigDecimal(0.01), new BigDecimal(0.01), new BigDecimal(1000_0000))
                        ) {
                            longList.add(hour4Candlesticks.get(hour4Candlesticks.size() - 1));
                        }

                        //空军
//                        if (1==1
////                                && isShunShiShort(monthCandlesticks, new BigDecimal(0.1), new BigDecimal(0.1), new BigDecimal(120_0000_0000))
//                                && isShunShiShort(weekCandlesticks, new BigDecimal(0.1), new BigDecimal(12_0000_0000))
//                                && isShunShiShort(dayCandlesticks, new BigDecimal(0.03), new BigDecimal(3_0000_0000))
//                                && isShunShiShort(hour4Candlesticks, new BigDecimal(0.02), new BigDecimal(6_000_0000))
////                                && isShunShiShort(hour1Candlesticks, new BigDecimal(0.015), new BigDecimal(2000_0000))
////                                && isShunShiShort(min30Candlesticks, new BigDecimal(0.01), new BigDecimal(1000_0000))
//                        ) {
//                            shortList.add(hour4Candlesticks.get(hour4Candlesticks.size() - 1));
//                        }

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

    /**
     * 顺势做多策略
     * @param candlesticks
     * @param jumpRateThreshold 振幅门槛值
     * @param usdtAmountThreshold 成交额门槛值
     * @return
     */
    public static boolean isShunShiLong(List<Candlestick> candlesticks, BigDecimal jumpRateThreshold
            , BigDecimal usdtAmountThreshold
            , Boolean isLong
            , Boolean isRight) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        Candlestick candlestick1 = candlesticks.size()>=2? candlesticks.get(candlesticks.size() - 2) : null;
        Candlestick candlestick2 = candlesticks.size()>=3? candlesticks.get(candlesticks.size() - 3) : null;
        return 1== 1

                // 成交额
                && (candlestick0.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0
                        || (candlestick1 == null || candlestick1.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0)
                        || (candlestick2 == null || candlestick2.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0))

                // 振幅
                && (candlestick0.getZhenFuRate().compareTo(jumpRateThreshold) > 0
                        || (candlestick1 != null && candlestick1.getZhenFuRate().compareTo(jumpRateThreshold) > 0)
                        || (candlestick2 != null && candlestick2.getZhenFuRate().compareTo(jumpRateThreshold) > 0))


                //本周期收红
                && (!isLong || candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) > 0)

                // 长线企稳
                && (candlestick2 == null
                        || candlestick0.getClose().compareTo(candlestick2.getLow()) > 0
                )

                && (candlestick1 == null
                        //上周期收红时，收与最高之上
                        || (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                                && candlestick0.getClose().compareTo(candlestick1.getLow()) > 0)

                        //上周期收绿时，本周期收红，且收盘价高于上周期、上2周期最低价
                        || (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                                && ((isRight && candlestick0.getShiTiRate().compareTo(candlestick1.getShiTiMax()) > 0)
                                    || (!isRight && candlestick0.getShiTiRate().compareTo(candlestick1.getLow()) > 0)
                                )))
                ;
    }



    /**
     * 顺势做空策略
     * @param candlesticks
     * @param jumpRateThreshold 振幅门槛值
     * @param usdtAmountThreshold 成交额门槛值
     * @return
     */
    public static boolean isShunShiShort(List<Candlestick> candlesticks, BigDecimal jumpRateThreshold, BigDecimal usdtAmountThreshold) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        Candlestick candlestick1 = candlesticks.size()>=2? candlesticks.get(candlesticks.size() - 2) : null;
        Candlestick candlestick2 = candlesticks.size()>=3? candlesticks.get(candlesticks.size() - 3) : null;
        return 1== 1

                // 成交额
                && (candlestick0.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0
                        || (candlestick1 == null || candlestick1.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0)
                        || (candlestick2 == null || candlestick2.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0))

                //振幅
                && (candlestick0.getZhenFuRate().compareTo(jumpRateThreshold) > 0
                        || (candlestick1 != null && candlestick1.getZhenFuRate().compareTo(jumpRateThreshold) > 0)
                        || (candlestick2 != null && candlestick2.getZhenFuRate().compareTo(jumpRateThreshold) > 0))


                //本周期收红
                && candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0

                // 上周期大红，或上周期收绿时，本周期反包
                && (candlestick1 == null
                        || candlestick1.getShiTiRate().compareTo(new BigDecimal("-0.01")) < 0
                        || candlestick0.getClose().compareTo(candlestick1.getShiTiMin()) < 0
                )

                // 长线企稳
                && (candlestick2 == null
                        || candlestick0.getClose().compareTo(candlestick2.getHigh()) < 0
                )

                && (candlestick1 == null
                        //上周期收红时，收与最高之上
                        || (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                                && candlestick0.getClose().compareTo(candlestick1.getHigh()) < 0)

                        //上周期收绿时，本周期收红，且收盘价高于上周期、上2周期最低价
                        || (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                                && candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                        )
                );
        }

}
