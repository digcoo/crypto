package com.binance.client.examples.predicate;

import com.binance.client.model.market.Candlestick;

import java.util.List;
import java.util.function.Predicate;

public class FanQushiPredicate implements Predicate<List<Candlestick>> {

    int limit = 2;

    public FanQushiPredicate(int limit) {
        this.limit = limit;
    }


    @Override
    public boolean test(List<Candlestick> candlesticks) {
        try {

            candlesticks = candlesticks.subList(limit-2, limit);

            Candlestick candlestick1 = candlesticks.get(0);
            Candlestick candlestick2 = candlesticks.get(1);
//            if ("RNDRUSDT".equals(candlestick2.getSymbol())) {
//                System.out.println("RNDRUSDT");
//            }

//        if (((candlestick1.getClose().compareTo(candlestick1.getOpen()) < 0 && candlestick2.getClose().compareTo(candlestick1.getOpen()) > 0)   //跌涨
//                  || (candlestick1.getClose().compareTo(candlestick1.getOpen()) > 0 && candlestick2.getClose().compareTo(candlestick1.getOpen()) < 0))  //涨跌
//                && candlestick2.getCloseTime() > System.currentTimeMillis()           //过滤停盘
//                && candlestick2.getQuoteAssetVolume().compareTo(new BigDecimal(200_000)) > 0           //过滤成交量
//        ) {
            if (((candlestick2.getClose().compareTo(candlestick2.getOpen()) > 0 && candlestick2.getClose().compareTo(candlestick1.getOpen()) > 0)
                    || (candlestick2.getClose().compareTo(candlestick2.getOpen()) < 0 && candlestick2.getClose().compareTo(candlestick1.getOpen()) < 0))

            ) {
//            System.out.println(symbol + "\t" + candlestick2.getQuoteAssetVolume() + "\t" + new Date() + "\t" + "https://www.binance.com/zh-CN/futures/"+symbol);
                return true;
            }

        } catch (Exception ex){
            ex.printStackTrace();
            System.out.println(candlesticks);
        }
        return false;
    }
}
