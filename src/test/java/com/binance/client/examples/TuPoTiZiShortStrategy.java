package com.binance.client.examples;


import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.dto.*;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ShortCandlestickMA;
import com.binance.client.model.market.ExchangeInformation;
import com.binance.client.utils.ShortStrategyUtil;
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
public class TuPoTiZiShortStrategy {

    public static final String API_KEY = "fAgJpcu289fMzPEP1YoeTz0Q4zOhly40d2vHoufpYu6oqiYxM2iUkCBeM0b2iVvG";
    public static final String SECRET_KEY = "IQcZ7D1N0l44cP61Xcw2LxTlSIK4zSV5uSgtKjKZZuleNfIXbvuJEGzAoGTMzeCQ"; // Unnecessary if PRIVATE_KEY_PATH is used



    public static void main(String[] args) {
        int limit = 100;  //取5条k数据
        BigDecimal zhenfuRate = new BigDecimal(0.5);  //振幅
        BigDecimal tradeAssetVolume = new BigDecimal(1_000_000);  //交易量
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
            try{

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                List<Candlestick> targetList = new ArrayList<>();

                /**
                 * 基本策略：
                 * 1、出现梯子
                 * 2、顺势而为
                 */
                for (String symbol: symbols) {
                    try {
//                        if ("REEFUSDT".equals(symbol)) {
//                            log.info("=======");
//                        }

                        List<Candlestick> dayCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.DAY.getInterval(), null, null, limit);
                        List<Candlestick> weekCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.WEEK.getInterval(), null, null, limit);
//                        List<Candlestick> monthCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MONTH.getInterval(), null, null, limit);

                        ShortCandlestickDTO dayCandlestickDTO = ShortStrategyUtil.buildShortCandlestickDTO(dayCandlesticks, PeriodTypeEnum.DAY);
                        ShortCandlestickDTO weekCandlestickDTO = ShortStrategyUtil.buildShortCandlestickDTO(weekCandlesticks, PeriodTypeEnum.WEEK);
//                        CandlestickInvertDTO monthCandlestickDTO = PriceUtil.buildCandlestickInvertDTO(monthCandlesticks, PeriodTypeEnum.MONTH);


                        ShortTiziCalculate tiziCalculate = new ShortTiziCalculate(dayCandlestickDTO.getCurrentCandlestick(), Arrays.asList(dayCandlestickDTO, weekCandlestickDTO));

                        boolean pass = ShortStrategyUtil.filterShortCandlestick(dayCandlestickDTO);

                        if (pass
                                && tiziCalculate.ifCrossTizi(dayCandlestickDTO.getLastCandlesticks(1))
//                                && checkFanbao(dayCandlestickDTO, false)
//                                && (checkHasTizi(dayCandlestickDTO, 4 , 2) //突破：一阳穿三
//                                        || checkCrossTizi(dayCandlestickDTO, 10 , 1)) //抄底：底突破
                        ) {
                            targetList.add(dayCandlesticks.get(dayCandlesticks.size() - 1));
                        }

                    }catch (Exception ex) {
                        log.error("symbol = {}", symbol, ex);
                    }
                }
                targetList.sort(Comparator.comparing(Candlestick::getQuoteAssetVolume).reversed());
//                for (Candlestick candlestick: targetList) {
//                    log.info("{}, {}, {}", candlestick.getSymbol(), candlestick.getQuoteAssetVolume(), "https://www.binance.com/zh-CN/futures/" + candlestick.getSymbol());
//                }
                stopWatch.stop();
                log.info("耗时[{}]秒", stopWatch.getTime()/1000);
            }catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    /**
     * {latestPeriods} 天有梯子，且梯子突破{topN}根【上/下】MA线，当前价格位于该梯子open上方
     * @param candlestickDTO
     * @param latestPeriods
     * @param topN
     * @return
     */
    private static boolean checkHasTizi(ShortCandlestickDTO candlestickDTO, Integer latestPeriods, Integer topN) {
        try {
            ShortCandlestickMA realtimeCandlestick = candlestickDTO.getCurrentCandlestick();
            List<ShortTizi> allTizis = findAllTizis(candlestickDTO, latestPeriods, topN);

            for (ShortTizi tizi: allTizis) {

                if (realtimeCandlestick.getClose().compareTo(tizi.getOpen()) < 0
                        && realtimeCandlestick.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                ){
                    return true;
                }
            }
        } catch (Exception ex) {
            log.error("[checkHasTizi] exception.....code = {}", candlestickDTO.getSymbol(), ex);
        }
        return false;
    }

    /**
     * {latestPeriods} 天有梯子，且梯子突破{topN}根【上/下】MA线，当前价格正在突破梯子open
     * @param candlestickDTO
     * @param latestPeriods
     * @param topN
     * @return
     */
    private static boolean checkCrossTizi(ShortCandlestickDTO candlestickDTO, Integer latestPeriods, Integer topN) {
        try {
            ShortCandlestickMA realtimeCandlestick = candlestickDTO.getCurrentCandlestick();
            List<ShortTizi> allTizis = findAllTizis(candlestickDTO, latestPeriods, topN);
            for (ShortTizi tizi: allTizis) {

//                if ("FXSUSDT".equals(tizi.getSymbol())) {
//                    log.info("{}, {} - {}, {} - {}", tizi.getSymbol(), tizi.getStartTrade().getOpenTimeStr(), tizi.getEndTrade().getCloseTimeStr(), tizi.getOpen(), tizi.getClose());
//                }

                if (realtimeCandlestick.getOpen().compareTo(tizi.getOpen()) > 0
                        && realtimeCandlestick.getClose().compareTo(tizi.getOpen()) < 0
                        && realtimeCandlestick.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                ){
                    return true;
                }
            }
        } catch (Exception ex) {
            log.error("[checkCrossTizi] exception.....code = {}", candlestickDTO.getSymbol(), ex);
        }
        return false;
    }


    private static List<ShortTizi> findAllTizis(ShortCandlestickDTO candlestickDTO, Integer latestPeriods, Integer topN) {
        List<ShortTizi> tizis = new ArrayList<>();
        if (candlestickDTO == null || candlestickDTO.getSize() == 0) {
            return tizis;
        }

        ShortCandlestickMA realtimeCandlestickMA = candlestickDTO.getCurrentCandlestick();

        try {
//            if ("FXSUSDT".equals(candlestickDTO.getSymbol())) {
//                log.info("{}", candlestickDTO.getSymbol());
//            }

            int i = 0;
            while (i < latestPeriods && i < candlestickDTO.getSize() - 1) {

                ShortCandlestickMA trade0 = candlestickDTO.getLastCandlestick(-i);
                ShortCandlestickMA tradeBefore1 = candlestickDTO.getLastCandlestick(-i - 1);
                ShortCandlestickMA tradeBefore2 = candlestickDTO.getLastCandlestick(-i - 2);

                trade0.setLastCandlestickMA(tradeBefore1);
//                log.info("{}, {}, {} - {}", trade0.getSymbol(), trade0.getCloseTimeStr(), trade0.getUpMA(topN).getLeft(), trade0.getUpMA(topN).getRight());
//                log.info("{}, {}, {} - {}", tradeBefore1.getSymbol(), tradeBefore1.getCloseTimeStr(), tradeBefore1.getUpMA(topN).getLeft(), tradeBefore1.getUpMA(topN).getRight());

                //上穿多MA线
                if ((trade0.ifCrossUpMA(topN) || trade0.ifCrossDownMA(topN))
                        //连续上涨，取第一根上涨
                        && (tradeBefore1.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                        || tradeBefore2 == null
                        || tradeBefore2.getShiTiRate().compareTo(BigDecimal.ZERO) > 0)
                ){
                    tizis.add(new ShortTizi(realtimeCandlestickMA.getSymbol(), Arrays.asList(tradeBefore1, trade0), null, null));
                }

                i++;
            }
        } catch (Exception ex) {
            log.error("[findAllTizis] exception.....code = {}", realtimeCandlestickMA.getSymbol(), ex);
        }
        return tizis;
    }

    private static boolean checkFanbao(ShortCandlestickDTO candlestickDTO, Boolean ifXiayi) {
        try {
            ShortCandlestickMA trade0 = candlestickDTO.getLastCandlestick(0);
            ShortCandlestickMA tradeBefore1 = candlestickDTO.getLastCandlestick(-1);
            return trade0.getClose().compareTo(tradeBefore1.getShiTiMin()) < 0
                    && (!ifXiayi || tradeBefore1.getShiTiRate().compareTo(BigDecimal.ZERO) > 0);
        }catch (Exception ex) {

        }

        return true;
    }



}
