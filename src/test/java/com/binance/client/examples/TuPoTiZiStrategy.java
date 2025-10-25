package com.binance.client.examples;


import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.dto.*;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ExchangeInformation;
import com.binance.client.model.market.LongCandlestickMA;
import com.binance.client.model.market.ShortCandlestickMA;
import com.binance.client.utils.LongStrategyUtil;
import com.binance.client.utils.ShortStrategyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 突破梯子做多策略
 *
 */
@Slf4j
public class TuPoTiZiStrategy {

    public static final String LINK_PREF = "https://www.binance.com/zh-CN/futures/";
    public static final String API_KEY = "fAgJpcu289fMzPEP1YoeTz0Q4zOhly40d2vHoufpYu6oqiYxM2iUkCBeM0b2iVvG";
    public static final String SECRET_KEY = "IQcZ7D1N0l44cP61Xcw2LxTlSIK4zSV5uSgtKjKZZuleNfIXbvuJEGzAoGTMzeCQ"; // Unnecessary if PRIVATE_KEY_PATH is used
    public static int limit = 50;  //取50条k数据

    //symbol -> long/short
    private static Map<String, Boolean> oldBidMap = new HashMap<>();

    static RequestOptions options = new RequestOptions();
    static SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
            options);

    public static void main(String[] args) {

        // print "hello world" string


        BigDecimal zhenfuRate = new BigDecimal(0.5);  //振幅
        BigDecimal tradeAssetVolume = new BigDecimal(1_000_000);  //交易量
//        CandlestickInterval period = CandlestickInterval.HOURLY;     //周期

        //获取所有合约交易对
        ExchangeInformation exchangeInformation = syncRequestClient.getExchangeInformation();
        List<String> symbols = exchangeInformation.getSymbols().stream().filter(x -> x.getSymbol().endsWith("USDT")).map(x -> x.getSymbol()).collect(Collectors.toList());
//        symbols.stream().forEach(x -> { System.out.println(x); });

        //定时拉取K线并判断
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {

                Map<String, Boolean> newBidMap = new HashMap<>();

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                /**
                 * 基本策略：
                 * 1、出现梯子
                 * 2、顺势而为
                 */
                for (String symbol : symbols) {
                    try {

                        Pair<String, Boolean> pair = checkCrossTizi(symbol, PeriodTypeEnum.HOUR4);
                        if (pair == null) {
                            continue;
                        }

                        if (pair.getRight()) {
                            if (!oldBidMap.keySet().contains(symbol)) {
                                log.warn("!!!!!!!!!!!!!!!!!!!多命中: {}, {}", symbol, LINK_PREF+symbol);
                            }else{
                                log.warn("多命中: {}, {}", symbol, LINK_PREF+symbol);
                            }
                        }else {
                            if (!oldBidMap.keySet().contains(symbol)) {
                                log.error("!!!!!!!!!!!!!!!!!!!空命中: {}, {}", symbol, LINK_PREF+symbol);
                            }else{
                                log.error("空命中: {}, {}", symbol, LINK_PREF+symbol);
                            }
                        }

                        newBidMap.put(symbol, pair.getRight());

                    } catch (Exception ex) {
                        log.error("symbol = {}", symbol, ex);
                    }
                }

                stopWatch.stop();
                log.info("耗时[{}]秒, 命中{}/{}", stopWatch.getTime() / 1000, newBidMap.size(), symbols.size());
//                log.info("未命中如下：");
//                for (String symbol : symbols) {
//                    if (!targetList.contains(symbol)) {
//                        log.info("未命中如下：{}\t{}", symbol, LINK_PREF + symbol);
//                    }
//                }

                oldBidMap.clear();
                oldBidMap.putAll(newBidMap);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0, 15, TimeUnit.MINUTES);
    }

    public static Pair<String, Boolean> checkCrossTizi(String symbol, PeriodTypeEnum periodTypeEnum) {

        if ("FTTUSDT".equals(symbol)) {
            log.debug("FTTUSDT");
        }

        LongCandlestickMA currentLongCandlestickMA = null;
        LongTiziCalculate longTiziCalculate = null;
        LongMACrossCalculate longMACrossCalculate = null;

        ShortCandlestickMA currentShortCandlestickMA = null;
        ShortTiziCalculate shortTiziCalculate = null;
        ShortMACrossCalculate shortMACrossCalculate = null;

        List<Candlestick> candlesticksT1= null;
        List<Candlestick> candlesticksT2 = null;
        List<Candlestick> candlesticksT3= null;
        List<Candlestick> candlesticksT4 = null;
        LongCandlestickDTO longCandlestickDTOT1 = null;
        LongCandlestickDTO longCandlestickDTOT2 = null;
        LongCandlestickDTO longCandlestickDTOT3 = null;
        ShortCandlestickDTO shortCandlestickDTOT1 = null;
        ShortCandlestickDTO shortCandlestickDTOT2 = null;
        ShortCandlestickDTO shortCandlestickDTOT3 = null;

        candlesticksT1 = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.HOUR4.getInterval(), null, null, limit);
        candlesticksT2 = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.DAY.getInterval(), null, null, limit);
        candlesticksT3 = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.WEEK.getInterval(), null, null, limit);
        candlesticksT4 = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MONTH.getInterval(), null, null, limit);

        if (candlesticksT1 == null || candlesticksT1.size() == 0 || candlesticksT1.get(0).getVolume().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        //顺势而为：警惕压力位收绿
        switch (periodTypeEnum){
            case HOUR4:
//                candlesticksT1 = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.HOUR4.getInterval(), null, null, limit);
//                candlesticksT2 = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.DAY.getInterval(), null, null, limit);

                //做多
                longCandlestickDTOT1 = LongStrategyUtil.buildLongCandlestickDTO(candlesticksT1, PeriodTypeEnum.HOUR4);
                longCandlestickDTOT2 = LongStrategyUtil.buildLongCandlestickDTO(candlesticksT2, PeriodTypeEnum.DAY);
                longCandlestickDTOT3 = LongStrategyUtil.buildLongCandlestickDTO(candlesticksT3, PeriodTypeEnum.WEEK);
                currentLongCandlestickMA = longCandlestickDTOT1.getCurrentCandlestick();
                longTiziCalculate = new LongTiziCalculate(currentLongCandlestickMA, Arrays.asList(longCandlestickDTOT1, longCandlestickDTOT2));
                longMACrossCalculate = new LongMACrossCalculate(currentLongCandlestickMA, Arrays.asList(longCandlestickDTOT1, longCandlestickDTOT2));

                //做空
                shortCandlestickDTOT1 = ShortStrategyUtil.buildShortCandlestickDTO(candlesticksT1, PeriodTypeEnum.HOUR4);
                shortCandlestickDTOT2 = ShortStrategyUtil.buildShortCandlestickDTO(candlesticksT2, PeriodTypeEnum.DAY);
                shortCandlestickDTOT3 = ShortStrategyUtil.buildShortCandlestickDTO(candlesticksT3, PeriodTypeEnum.WEEK);
                currentShortCandlestickMA = shortCandlestickDTOT1.getCurrentCandlestick();
                shortTiziCalculate = new ShortTiziCalculate(currentShortCandlestickMA, Arrays.asList(shortCandlestickDTOT1, shortCandlestickDTOT2));
                shortMACrossCalculate = new ShortMACrossCalculate(currentShortCandlestickMA, Arrays.asList(shortCandlestickDTOT1, shortCandlestickDTOT2));

                break;

            case DAY:

//                candlesticksT1 = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.DAY.getInterval(), null, null, limit);
//                candlesticksT2 = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.WEEK.getInterval(), null, null, limit);

                //做多
                longCandlestickDTOT1 = LongStrategyUtil.buildLongCandlestickDTO(candlesticksT2, PeriodTypeEnum.DAY);
                longCandlestickDTOT2 = LongStrategyUtil.buildLongCandlestickDTO(candlesticksT3, PeriodTypeEnum.WEEK);
                longCandlestickDTOT3 = LongStrategyUtil.buildLongCandlestickDTO(candlesticksT4, PeriodTypeEnum.MONTH);
                currentLongCandlestickMA = longCandlestickDTOT1.getCurrentCandlestick();
                longTiziCalculate = new LongTiziCalculate(currentLongCandlestickMA, Arrays.asList(longCandlestickDTOT2, longCandlestickDTOT2));
                longMACrossCalculate = new LongMACrossCalculate(currentLongCandlestickMA, Arrays.asList(longCandlestickDTOT1, longCandlestickDTOT2));

                //做空
                shortCandlestickDTOT1 = ShortStrategyUtil.buildShortCandlestickDTO(candlesticksT2, PeriodTypeEnum.DAY);
                shortCandlestickDTOT2 = ShortStrategyUtil.buildShortCandlestickDTO(candlesticksT3, PeriodTypeEnum.WEEK);
                shortCandlestickDTOT3 = ShortStrategyUtil.buildShortCandlestickDTO(candlesticksT4, PeriodTypeEnum.MONTH);
                currentShortCandlestickMA = shortCandlestickDTOT1.getCurrentCandlestick();
                shortTiziCalculate = new ShortTiziCalculate(currentShortCandlestickMA, Arrays.asList(shortCandlestickDTOT1, shortCandlestickDTOT2));
                shortMACrossCalculate = new ShortMACrossCalculate(currentShortCandlestickMA, Arrays.asList(shortCandlestickDTOT1, shortCandlestickDTOT2));

                break;
            case WEEK:
            case MONTH:

//                candlesticksT1 = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.WEEK.getInterval(), null, null, limit);
//                candlesticksT2 = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MONTH.getInterval(), null, null, limit);

                //做多
                longCandlestickDTOT1 = LongStrategyUtil.buildLongCandlestickDTO(candlesticksT3, PeriodTypeEnum.WEEK);
                longCandlestickDTOT2 = LongStrategyUtil.buildLongCandlestickDTO(candlesticksT4, PeriodTypeEnum.MONTH);
                currentLongCandlestickMA = longCandlestickDTOT1.getCurrentCandlestick();
                longTiziCalculate = new LongTiziCalculate(currentLongCandlestickMA, Arrays.asList(longCandlestickDTOT1, longCandlestickDTOT2));
                longTiziCalculate = new LongTiziCalculate(currentLongCandlestickMA, Arrays.asList(longCandlestickDTOT1, longCandlestickDTOT2));

                //做空
                shortCandlestickDTOT1 = ShortStrategyUtil.buildShortCandlestickDTO(candlesticksT3, PeriodTypeEnum.WEEK);
                shortCandlestickDTOT2 = ShortStrategyUtil.buildShortCandlestickDTO(candlesticksT4, PeriodTypeEnum.MONTH);
                currentShortCandlestickMA = shortCandlestickDTOT1.getCurrentCandlestick();
                shortTiziCalculate = new ShortTiziCalculate(currentShortCandlestickMA, Arrays.asList(shortCandlestickDTOT1, shortCandlestickDTOT2));
                shortMACrossCalculate = new ShortMACrossCalculate(currentShortCandlestickMA, Arrays.asList(shortCandlestickDTOT1, shortCandlestickDTOT2));

                break;
        }

        boolean longPass = LongStrategyUtil.filterLongCandlestick(longCandlestickDTOT1, longCandlestickDTOT2, longCandlestickDTOT3);
        boolean shortPass = ShortStrategyUtil.filterShortCandlestick(shortCandlestickDTOT1, shortCandlestickDTOT2, shortCandlestickDTOT3);

        if (longPass) {
            return Pair.of(symbol, true);
        }else if (shortPass) {
            return Pair.of(symbol, false);
        }
        return null;

//        return (longPass && longTiziCalculate.ifCrossTizi(Arrays.asList(currentLongCandlestickMA)))
//                || (shortPass && shortTiziCalculate.ifCrossTizi(Arrays.asList(currentShortCandlestickMA)))
//            || (longPass && longMACrossCalculate.ifCrossMACross(longCandlestickDTOT1.getLastCandlesticks(1)))
//                || (shortPass && shortMACrossCalculate.ifCrossMACross(shortCandlestickDTOT1.getLastCandlesticks(1)));
    }

}
