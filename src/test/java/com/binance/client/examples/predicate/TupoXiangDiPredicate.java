package com.binance.client.examples.predicate;

import com.binance.client.model.market.Candlestick;

import java.util.List;
import java.util.function.Predicate;

public class TupoXiangDiPredicate implements Predicate<List<Candlestick>> {

    int limit = 2;

    public TupoXiangDiPredicate(int limit) {
        this.limit = limit;
    }


    @Override
    public boolean test(List<Candlestick> candlesticks) {
        try {

            candlesticks = candlesticks.subList(limit-5, limit);

            Candlestick candlestick1 = candlesticks.get(0);
            Candlestick candlestick2 = candlesticks.get(1);
            Candlestick candlestick3 = candlesticks.get(2);
            Candlestick candlestick4 = candlesticks.get(3);
            Candlestick candlestick5 = candlesticks.get(4);


//            if ("RNDRUSDT".equals(candlestick2.getSymbol())) {
//                System.out.println("RNDRUSDT");
//            }

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
                            (candlestick3.getClose().compareTo(candlestick3.getOpen()) > 0      //第3天收涨
                                    && candlestick5.getLow().compareTo(candlestick3.getOpen()) < 0 && candlestick5.getClose().compareTo(candlestick3.getClose()) > 0)
                    )
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
