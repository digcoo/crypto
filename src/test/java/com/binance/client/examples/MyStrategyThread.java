package com.binance.client.examples;


import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.enums.SideTypeEnum;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ExchangeInformation;
import com.binance.client.utils.CheckValidator;
import com.binance.client.utils.IndicatorCaculater;
import com.binance.client.utils.MessageSenderUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 顺势策略
 *
 */
public class MyStrategyThread extends Thread{

    Logger log = LoggerFactory.getLogger(this.getClass().getClass());

    public static final String API_KEY = "fAgJpcu289fMzPEP1YoeTz0Q4zOhly40d2vHoufpYu6oqiYxM2iUkCBeM0b2iVvG";
    public static final String SECRET_KEY = "IQcZ7D1N0l44cP61Xcw2LxTlSIK4zSV5uSgtKjKZZuleNfIXbvuJEGzAoGTMzeCQ"; // Unnecessary if PRIVATE_KEY_PATH is used
    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";
    Map<String, KLine> klineMap;

    List<KLine> longKLines;
    List<KLine> shortKLines;

    MyStrategyThread(Map<String, KLine> klineMap, List<KLine>  longKLines, List<KLine>  shortKLines){
        this.klineMap = klineMap;
        this.longKLines = longKLines;
        this.shortKLines = shortKLines;
    }

    //覆盖run方法
    @Override
    public void run() {
        log.info("开始执行策略");
        int limit = 150;  //取5条k数据
//        BigDecimal zhenfuRate = new BigDecimal(0.5);  //振幅
//        BigDecimal tradeAssetVolume = new BigDecimal(1_000_000);  //交易量
//        CandlestickInterval period = CandlestickInterval.HOURLY;     //周期

        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);

        //获取所有合约交易对
        ExchangeInformation exchangeInformation = syncRequestClient.getExchangeInformation();
        List<String> symbols = exchangeInformation.getSymbols().stream().filter(x -> x.getSymbol().endsWith("USDT")).map(x -> x.getSymbol()).collect(Collectors.toList());

        //定时拉取K线并判断
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                /**
                 * 基本策略：
                 * 1、出现梯子
                 * 2、顺势而为
                 */
                for (String symbol : symbols) {

                    if (!symbol.contains("BTC")) {
                        continue;
                    }

                    try {
//                        List<SymbolOrderBook> symbolOrderBookTicker = syncRequestClient.getSymbolOrderBookTicker(symbol);
                        List<Candlestick> monthCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MONTH.getInterval(), null, null, limit);
                        List<Candlestick> weekCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.WEEK.getInterval(), null, null, limit);
                        List<Candlestick> dayCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.DAY.getInterval(), null, null, limit);
                        List<Candlestick> hour4Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.HOUR4.getInterval(), null, null, limit);
                        List<Candlestick> min30Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MIN30.getInterval(), null, null, limit);
                        List<Candlestick> min5Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MIN5.getInterval(), null, null, limit);
                        klineMap.put(symbol, new KLine(symbol, monthCandlesticks, weekCandlesticks, dayCandlesticks, hour4Candlesticks, min30Candlesticks, min5Candlesticks));
                    } catch (Exception ex) {
                        log.error("symbol = {}", symbol, ex);
                    }

                }

                this.longKLines.clear();
                this.shortKLines.clear();


                //1h=5_000U(1秒) * 60（1分钟） * 60（1小时）=18_00_0000 ~ 2000_0000
                //4h= 4 * 18_00_0000 =  72_00_0000 ~ 8000_0000
                //1d= 6 * 4 * 18_00_0000 =  6 ^ 8000_0000 = 4_0000_0000

                for (String symbol : symbols) {

                    KLine kline = klineMap.get(symbol);
                    Candlestick min5Candlestick0 = kline.getM5Lines().get(kline.getM5Lines().size() - 1);
                    Candlestick min30Candlestick0 = kline.getM30Lines().get(kline.getM30Lines().size() - 1);

                    if (!checkBase(kline.getH4Lines(), new BigDecimal("0.01"), new BigDecimal(1000_0000))) {
                        continue;
                    }

                    //多军
                    if (
                            CheckValidator.checkOverMA(kline.getM30Lines(), Arrays.asList(7, 14), SideTypeEnum.LONG)
                             //主升浪指标：共振
                             && CheckValidator.checkMAGongzhen(kline.getM30Lines(), Arrays.asList(7, 14), SideTypeEnum.LONG)
                            //上移
                            && CheckValidator.checkShangyi(kline.getM30Lines(), SideTypeEnum.LONG)
                    ) {
                        longKLines.add(kline);
                        log.warn("多头命中: {}", link_pref + symbol);
                        MessageSenderUtil.send(symbol+"开多");
                    }

                    if (
                            CheckValidator.checkOverMA(kline.getM30Lines(), Arrays.asList(7, 14), SideTypeEnum.SHORT)
                            //主升浪指标：共振
                            && CheckValidator.checkMAGongzhen(kline.getM30Lines(), Arrays.asList(7, 14), SideTypeEnum.SHORT)
                            //上移
                            && CheckValidator.checkShangyi(kline.getM30Lines(), SideTypeEnum.SHORT)
                    ) {
                        shortKLines.add(kline);
                        log.warn("空头命中: {}", link_pref + symbol);
                        MessageSenderUtil.send(symbol+"开空");
                    }
                }

                stopWatch.stop();

                log.warn("多头命中[{}/{}]", longKLines.size(), symbols.size());
                log.error("空头命中[{}/{}]", shortKLines.size(), symbols.size());

                log.info("总耗时[{}]秒", stopWatch.getTime() / 1000);


            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0, 2, TimeUnit.MINUTES);
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
//                && (candlestick2 == null
//                        || candlestick0.getClose().compareTo(candlestick2.getLow()) > 0
//                )

                && (candlestick1 == null
                        //上周期收红时，收与最高之上
                        || (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                                && candlestick0.getClose().compareTo(candlestick1.getLow()) > 0)

                        //上周期收绿时，本周期收红，且收盘价高于上周期、上2周期最低价
                        || (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                                && ((isRight && candlestick0.getClose().compareTo(candlestick1.getShiTiMax()) > 0)
                                    || (!isRight && candlestick0.getClose().compareTo(candlestick1.getLow()) > 0)
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
    public static boolean isShunShiShort(List<Candlestick> candlesticks, BigDecimal jumpRateThreshold
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

                //振幅
                && (candlestick0.getZhenFuRate().compareTo(jumpRateThreshold) > 0
                        || (candlestick1 != null && candlestick1.getZhenFuRate().compareTo(jumpRateThreshold) > 0)
                        || (candlestick2 != null && candlestick2.getZhenFuRate().compareTo(jumpRateThreshold) > 0))


                //本周期收红
                && (!isLong || candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0)

                // 长线企稳
//                && (candlestick2 == null
//                        || candlestick0.getClose().compareTo(candlestick2.getHigh()) < 0
//                )


                && (candlestick1 == null
                        //上周期收红时，收与最高之上
                        || (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                                && candlestick0.getClose().compareTo(candlestick1.getHigh()) < 0)

                        //上周期收绿时，本周期收红，且收盘价高于上周期、上2周期最低价
                        || (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                                && ((isRight && candlestick0.getClose().compareTo(candlestick1.getShiTiMin()) < 0)
                                    || (!isRight && candlestick0.getClose().compareTo(candlestick1.getHigh()) < 0)
                )))
                ;
        }



    /**
     * 大比例突破柱子
     * @param candlesticks
     * @return
     */
    public static boolean isCrossZhongshuLong(List<Candlestick> candlesticks, BigDecimal zhuziShitiRate, BigDecimal shitiRate) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        Candlestick candlestick1 = candlesticks.size()>=2? candlesticks.get(candlesticks.size() - 2) : null;
        Candlestick candlestick2 = candlesticks.size()>=3? candlesticks.get(candlesticks.size() - 3) : null;
        for (int i = candlesticks.size() - 1; i >= 0; i--) {
            Candlestick candlestick = candlesticks.get(i);

            //本周期突破
            if (candlestick.getShiTiRate().compareTo(zhuziShitiRate) > 0
                && candlestick0.getShiTiRate().compareTo(shitiRate) > 0
                && candlestick0.getClose().compareTo(candlestick.getOpen()) > 0
                && candlestick1.getClose().compareTo(candlestick.getOpen()) < 0) {

                return true;
            }

            //上周期突破
            if (candlestick.getShiTiRate().compareTo(zhuziShitiRate) > 0
                    && candlestick0.getClose().compareTo(candlestick.getOpen()) > 0
                    && candlestick0.getClose().compareTo(candlestick0.getOpen()) > 0
                    && candlestick1.getShiTiRate().compareTo(shitiRate) > 0
                    && candlestick1.getClose().compareTo(candlestick.getOpen()) > 0
                    && candlestick2.getClose().compareTo(candlestick.getOpen()) < 0) {

                return true;
            }

        }
        return false;
    }

    /**
     * 大比例突破柱子
     * @param candlesticks
     * @return
     */
    public static boolean isCrossZhongshuShort(List<Candlestick> candlesticks, BigDecimal zhuziShitiRate, BigDecimal shitiRate) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        Candlestick candlestick1 = candlesticks.size()>=2? candlesticks.get(candlesticks.size() - 2) : null;
        Candlestick candlestick2 = candlesticks.size()>=3? candlesticks.get(candlesticks.size() - 3) : null;
        for (int i = candlesticks.size() - 1; i >= 0; i--) {
            Candlestick candlestick = candlesticks.get(i);

            //本周期突破
            if (candlestick.getShiTiRate().compareTo(zhuziShitiRate) < 0
                    && candlestick0.getShiTiRate().compareTo(shitiRate) < 0
                    && candlestick0.getClose().compareTo(candlestick.getOpen()) < 0
                    && candlestick1.getClose().compareTo(candlestick.getOpen()) > 0) {
                return true;
            }



            //上周期突破
            if (candlestick.getShiTiRate().compareTo(zhuziShitiRate) < 0
                    && candlestick0.getClose().compareTo(candlestick.getOpen()) < 0
                    && candlestick0.getClose().compareTo(candlestick0.getOpen()) < 0
                    && candlestick1.getShiTiRate().compareTo(shitiRate) < 0
                    && candlestick1.getClose().compareTo(candlestick.getOpen()) < 0
                    && candlestick2.getClose().compareTo(candlestick.getOpen()) > 0) {

                return true;
            }
        }
        return false;
    }


    /**
     * 校验成交量和振幅
     * @param candlesticks
     * @param zhenfuRate 振幅门槛值
     * @param usdtAmountThreshold 成交额门槛值
     * @return
     */
    public static boolean checkBase(List<Candlestick> candlesticks, BigDecimal zhenfuRate, BigDecimal usdtAmountThreshold) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        Candlestick candlestick1 = candlesticks.size() >= 2 ? candlesticks.get(candlesticks.size() - 2) : null;
        Candlestick candlestick2 = candlesticks.size() >= 3 ? candlesticks.get(candlesticks.size() - 3) : null;
        return
                // 成交额
                (candlestick0.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0
                    || (candlestick1 == null || candlestick1.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0)
                    || (candlestick2 == null || candlestick2.getQuoteAssetVolume().compareTo(usdtAmountThreshold) > 0))

                // 振幅
                && (candlestick0.getZhenFuRate().compareTo(zhenfuRate) > 0
                    || (candlestick1 != null && candlestick1.getZhenFuRate().compareTo(zhenfuRate) > 0)
                    || (candlestick2 != null && candlestick2.getZhenFuRate().compareTo(zhenfuRate) > 0))
                ;
    }

    /**
     * 顺势做多策略
     * @param candlesticks
     * @param shitiRateThreshold 振幅门槛值
     * @param shitiRateThreshold 成交额门槛值
     * @return
     */
    public static boolean checkTupoLong(List<Candlestick> candlesticks, int latestPeriodNum, BigDecimal shitiRateThreshold) {
        candlesticks = candlesticks.subList(candlesticks.size() - latestPeriodNum, candlesticks.size() - 1);
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        for (int i = candlesticks.size() - 1; i >= 0; i--) {
            Candlestick tmpCandlestick = candlesticks.get(i);
            if (tmpCandlestick.getShiTiRate().compareTo(shitiRateThreshold) > 0
                    && candlestick0.getClose().compareTo(tmpCandlestick.getLow()) > 0) {
                return true;
            }
        }
        return false;
    }


    /**
     * 顺势做多策略
     * @param candlesticks
     * @param latestPeriodNum 振幅门槛值
     * @param shitiRateThreshold 成交额门槛值
     * @return
     */
    public static boolean checkTupoShort(List<Candlestick> candlesticks, int latestPeriodNum, BigDecimal shitiRateThreshold) {
        candlesticks = candlesticks.subList(candlesticks.size() - latestPeriodNum, candlesticks.size() - 1);
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
        for (int i = candlesticks.size() - 1; i >= 0; i--) {
            Candlestick tmpCandlestick = candlesticks.get(i);
            if (tmpCandlestick.getShiTiRate().compareTo(shitiRateThreshold) < 0
                    && candlestick0.getClose().compareTo(tmpCandlestick.getHigh()) < 0) {
                return true;
            }
        }
        return false;
    }

}
