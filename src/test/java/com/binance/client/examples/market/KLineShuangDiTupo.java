package com.binance.client.examples.market;


import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ExchangeInformation;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KLineShuangDiTupo {

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

            symbols.stream().forEach(symbol -> {

                //获取K线
                List<Candlestick> candlesticks = syncRequestClient.getCandlestick(symbol, CandlestickInterval.FIVE_MINUTES, null, null, 3);

                //是否触发条件
                if (isShuangdiTupo(candlesticks, symbol)){
//                    System.out.println("===========");
                }

//                candlesticks.stream().forEach(x -> { System.out.println(x); });

            });
            System.out.println("\n\n\n");
        }, 0, 1, TimeUnit.MINUTES);
    }


    /**
     * 双底突破
     * @param candlesticks
     * @param symbol
     * @return
     */
    private static Boolean isShuangdiTupo(List<Candlestick> candlesticks, String symbol){
        Candlestick candlestick1 = candlesticks.get(0);
        Candlestick candlestick2 = candlesticks.get(1);
        Candlestick candlestick3 = candlesticks.get(2);


        if (((candlestick1.getClose().compareTo(candlestick1.getOpen()) > 0 && candlestick2.getClose().compareTo(candlestick2.getOpen()) < 0 && candlestick3.getClose().compareTo(candlestick1.getClose()) > 0)   //涨跌涨
                  || (candlestick1.getClose().compareTo(candlestick1.getOpen()) < 0 && candlestick2.getClose().compareTo(candlestick2.getOpen()) > 0 && candlestick3.getClose().compareTo(candlestick1.getClose()) < 0))  //跌涨跌

//                && candlestick3.getCloseTime() > System.currentTimeMillis()           //过滤停盘
                && candlestick3.getQuoteAssetVolume().compareTo(new BigDecimal(300_000)) > 0           //过滤成交量
        ) {
            System.out.println(symbol + "\t" + candlestick3.getQuoteAssetVolume() + "\t" + new Date() + "\t" + "https://www.binance.com/zh-CN/futures/"+symbol);
            return true;
        }

        return false;

    }

}
