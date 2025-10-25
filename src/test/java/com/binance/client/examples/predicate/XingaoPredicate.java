package com.binance.client.examples.predicate;

import com.binance.client.model.market.Candlestick;

import java.util.List;
import java.util.function.Predicate;

public class XingaoPredicate implements Predicate<List<Candlestick>> {

    int totalSize = 2;

    public XingaoPredicate(int totalSize) {
        this.totalSize = totalSize;
    }


    @Override
    public boolean test(List<Candlestick> candlesticks) {
        try {

            candlesticks = candlesticks.subList(totalSize-8, totalSize);

            Candlestick latestCandlestick = candlesticks.get(candlesticks.size() - 1);

//            if ("RNDRUSDT".equals(candlestick2.getSymbol())) {
//                System.out.println("RNDRUSDT");
//            }


            if (latestCandlestick.getClose().doubleValue()
                    > candlesticks.subList(0, candlesticks.size() - 2).stream().mapToDouble(x -> x.getClose().doubleValue()).max().getAsDouble()

            ) {
                return true;
            }

        } catch (Exception ex){
            ex.printStackTrace();
            System.out.println(candlesticks);
        }
        return false;
    }
}
