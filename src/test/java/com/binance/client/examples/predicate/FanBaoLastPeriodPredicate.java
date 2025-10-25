package com.binance.client.examples.predicate;

import com.binance.client.model.market.Candlestick;

import java.util.List;
import java.util.function.Predicate;

public class FanBaoLastPeriodPredicate implements Predicate<List<Candlestick>> {

    int limit = 3;

    public FanBaoLastPeriodPredicate(int limit) {
        this.limit = limit;
    }


    @Override
    public boolean test(List<Candlestick> candlesticks) {
        try {

            candlesticks = candlesticks.subList(limit-3, limit);

            Candlestick candlestick1 = candlesticks.get(0);
            Candlestick candlestick2 = candlesticks.get(1);
            Candlestick candlestick3 = candlesticks.get(2);

//            if ("RNDRUSDT".equals(candlestick3.getSymbol())) {
//                System.out.println("RNDRUSDT");
//            }

            if (((candlestick2.getClose().compareTo(candlestick2.getOpen()) > 0 && candlestick2.getClose().compareTo(candlestick1.getOpen()) > 0)
                    || (candlestick2.getClose().compareTo(candlestick2.getOpen()) < 0 && candlestick2.getClose().compareTo(candlestick1.getOpen()) < 0))
            ) {
                return true;
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }

        return false;
    }
}
