package com.binance.client.examples.predicate;

import com.binance.client.model.market.Candlestick;

import java.util.List;
import java.util.function.Predicate;

/**
 * 最近有突破分析
 */
public class LatestFanbaoPredicate implements Predicate<List<Candlestick>> {

    int limit = 6;

    public LatestFanbaoPredicate(int limit) {
        this.limit = limit;
    }


    @Override
    public boolean test(List<Candlestick> candlesticks) {
        try {

            candlesticks = candlesticks.subList(limit-6, limit);

            Candlestick candlestick1 = candlesticks.get(0);
            Candlestick candlestick2 = candlesticks.get(1);
            Candlestick candlestick3 = candlesticks.get(2);
            Candlestick candlestick4 = candlesticks.get(3);
            Candlestick candlestick5 = candlesticks.get(4);
            Candlestick candlestick6 = candlesticks.get(5);


//            if ("RNDRUSDT".equals(candlestick2.getSymbol())) {
//                System.out.println("RNDRUSDT");
//            }

            //1天反包，2天反包，3天反包
            if (isLatestTuPo(candlesticks, candlestick6)){
                return true;
            }

            return false;


        } catch (Exception ex){
            ex.printStackTrace();
            System.out.println(candlesticks);
        }
        return false;
    }

    private boolean isLatestTuPo(List<Candlestick> candlesticks, Candlestick nowCandlestick) {
        for (int i = 0; i < candlesticks.size(); i++) {
            Candlestick cursor = candlesticks.get(i);
            if (isLatestTuPo(candlesticks.subList(i + 1, candlesticks.size()), cursor, nowCandlestick) ) {
                return true;
            }
        }
        return false;
    }


    private boolean isLatestTuPo(List<Candlestick> candlesticks, Candlestick cursor, Candlestick nowCandlestick) {

        if (cursor.getClose().compareTo(cursor.getOpen()) < 0) {
//            if (!(nowCandlestick.getClose().compareTo(nowCandlestick.getOpen()) > 0)){      //突破与当前价格趋势要一致
//                return false;
//            }
            for (Candlestick candlestick: candlesticks) {
                if (candlestick.getClose().compareTo(candlestick.getOpen()) < 0){
                    return false;
                }

                if (candlestick.getClose().compareTo(cursor.getOpen()) > 0) {
                    return true;
                }
            }
        }else {
//            if (!(nowCandlestick.getClose().compareTo(nowCandlestick.getOpen()) < 0)){
//                return false;
//            }

            for (Candlestick candlestick: candlesticks) {
                if (candlestick.getClose().compareTo(candlestick.getOpen()) > 0){
                    return false;
                }

                if (candlestick.getClose().compareTo(cursor.getOpen()) < 0) {
                    return true;
                }
            }
        }
        return false;

    }

}
