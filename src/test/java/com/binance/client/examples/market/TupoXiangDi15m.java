package com.binance.client.examples.market;


import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ExchangeInformation;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TupoXiangDi15m {

    public static final String API_KEY = "fAgJpcu289fMzPEP1YoeTz0Q4zOhly40d2vHoufpYu6oqiYxM2iUkCBeM0b2iVvG";
    public static final String SECRET_KEY = "IQcZ7D1N0l44cP61Xcw2LxTlSIK4zSV5uSgtKjKZZuleNfIXbvuJEGzAoGTMzeCQ"; // Unnecessary if PRIVATE_KEY_PATH is used


    public static void main(String[] args) {

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
            symbols.stream()
                    .map(symbol -> Pair.of(symbol, syncRequestClient.getCandlestick(symbol, CandlestickInterval.FIFTEEN_MINUTES, null, null, 5)))
                    .filter(pair -> isTriggerTupoXiangDi(pair.getRight(), pair.getLeft()))
                    .map(pair -> pair.getRight().get(pair.getRight().size() - 1).setSymbol(pair.getLeft())) // 取倒数第二条数据振幅
                    .sorted(new Comparator<Candlestick>() {
                        @Override
                        public int compare(Candlestick o1, Candlestick o2) {
                            return o2.getZhenFuRate().compareTo(o1.getZhenFuRate());
                        }
                    }).forEach(candlestick ->
                            System.out.println(candlestick.getSymbol() + "\t" + candlestick.getZhenFuRate() + "\t" + candlestick.getQuoteAssetVolume() + "\t" + new Date() + "\t" + "https://www.binance.com/zh-CN/futures/"+candlestick.getSymbol())
                    );
            System.out.println("\n");

//            symbols.stream().forEach(symbol -> {
//
//                //获取K线
//                List<Candlestick> candlesticks = syncRequestClient.getCandlestick(symbol, CandlestickInterval.ONE_MINUTE, null, null, 5);
//
//                //是否触发条件
//                if (isTriggerZhenfu(candlesticks, symbol)){
////                    System.out.println("===========");
//                }
//
////                candlesticks.stream().forEach(x -> { System.out.println(x); });
//
//            });
//            System.out.println("\n\n\n");
        }, 0, 30, TimeUnit.SECONDS);
    }


    /**
     * 反包
     * @param candlesticks
     * @param symbol
     * @return
     */
    private static Boolean isTriggerTupoXiangDi(List<Candlestick> candlesticks, String symbol){
        Candlestick candlestick1 = candlesticks.get(0);
        Candlestick candlestick2 = candlesticks.get(1);
        Candlestick candlestick3 = candlesticks.get(2);
        Candlestick candlestick4 = candlesticks.get(3);
        Candlestick candlestick5 = candlesticks.get(4);

        if (1 == 1
//                && candlestick4.getClose().compareTo(candlestick4.getOpen()) < 0    //前一个收跌
                && candlestick5.getClose().compareTo(candlestick5.getOpen()) > 0    //当前收涨
                && candlestick5.getClose().compareTo(candlestick4.getClose()) > 0   //当前价高于昨日价
                && (
                    (candlestick1.getClose().compareTo(candlestick1.getOpen()) > 0      //第1天收涨
                      && candlestick5.getLow().compareTo(candlestick1.getOpen()) < 0 && candlestick5.getClose().compareTo(candlestick1.getClose()) > 0)
                     ||
                    (candlestick2.getClose().compareTo(candlestick2.getOpen()) > 0      //第2天收涨
                      && candlestick5.getLow().compareTo(candlestick2.getOpen()) < 0 && candlestick5.getClose().compareTo(candlestick2.getClose()) > 0)
                     ||
                    (candlestick3.getClose().compareTo(candlestick3.getOpen()) > 0      //第2天收涨
                      && candlestick5.getLow().compareTo(candlestick3.getOpen()) < 0 && candlestick5.getClose().compareTo(candlestick3.getClose()) > 0)
                )

                && candlestick1.getQuoteAssetVolume().compareTo(new BigDecimal(200_000)) > 0           //过滤成交量
        ) {
//            System.out.println(symbol + "\t" + candlestick2.getQuoteAssetVolume() + "\t" + new Date() + "\t" + "https://www.binance.com/zh-CN/futures/"+symbol);
            return true;
        }

        return false;

    }

}
