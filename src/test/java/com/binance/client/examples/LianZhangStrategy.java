package com.binance.client.examples;


import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.examples.predicate.FanBaoLastPeriodPredicate;
import com.binance.client.examples.predicate.FanBaoPredicate;
import com.binance.client.examples.predicate.LianZhangMapFunction;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ExchangeInformation;
import org.apache.commons.lang3.time.StopWatch;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LianZhangStrategy {

    public static final String API_KEY = "fAgJpcu289fMzPEP1YoeTz0Q4zOhly40d2vHoufpYu6oqiYxM2iUkCBeM0b2iVvG";
    public static final String SECRET_KEY = "IQcZ7D1N0l44cP61Xcw2LxTlSIK4zSV5uSgtKjKZZuleNfIXbvuJEGzAoGTMzeCQ"; // Unnecessary if PRIVATE_KEY_PATH is used



    public static void main(String[] args) {
        int limit = 5;  //取5条k数据
        BigDecimal zhenfuRate = new BigDecimal(0.5);  //振幅
        BigDecimal tradeAssetVolume = new BigDecimal(1_000_000);  //交易量
        CandlestickInterval period = CandlestickInterval.HOURLY;     //周期


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
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            /**
             * 基本策略：
             * 1、硬指标：1h的振幅 > 1%
             * 2、硬原则：单边形成的条件（底部长时间的震荡、大跌形成的空间）
             * 3、1h+定趋势：单边/箱体（单边：本周期的单边、大周期的位置；箱体：本周期的箱体，大周期的位置）
             * 4、特点选股 ：15min反包
             * 5、顺势而为
             */
            symbols.stream()
                    //获取K线数据
                    .map(symbol -> syncRequestClient.getCandlestick(symbol, period, null, null, limit))
                    .filter(candlesticks -> candlesticks.size() >= limit)
                    .filter(new FanBaoPredicate(limit).or(new FanBaoLastPeriodPredicate(limit)))
                    //震荡区连涨，大概率要跌
                    .map(candlesticks -> new LianZhangMapFunction(zhenfuRate, tradeAssetVolume, limit).apply(candlesticks))
                    .filter(Objects::nonNull)
                    .sorted(new Comparator<Candlestick>() {
                        @Override
                        public int compare(Candlestick o1, Candlestick o2) {
                            return o1.getZhenFuRate().compareTo(o2.getZhenFuRate());
                        }
                    })
                    .forEach(candlestick ->
                            System.out.println(candlestick.getSymbol() + "\t" + candlestick.getZhenFuRate() + "\t" + "https://www.binance.com/zh-CN/futures/"+candlestick.getSymbol())
                    );
            stopWatch.stop();
            System.out.println("耗时[" + stopWatch.getTime()/1000 + "]秒\n\n\n");

        }, 0, 5, TimeUnit.MINUTES);
    }

}
