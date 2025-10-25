package com.binance.client.examples.predicate;

import com.binance.client.model.market.Candlestick;

import java.util.List;
import java.util.function.Predicate;

public class ShunshiPredicate implements Predicate<List<Candlestick>> {

    int totalSize = 2;

    public ShunshiPredicate(int totalSize) {
        this.totalSize = totalSize;
    }


    @Override
    public boolean test(List<Candlestick> candlesticks) {
        try {

            candlesticks = candlesticks.subList(totalSize-4, totalSize);

            Candlestick candlestick1 = candlesticks.get(0);
            Candlestick candlestick2 = candlesticks.get(1);
            Candlestick candlestick3 = candlesticks.get(2);
            Candlestick candlestick4 = candlesticks.get(3);
//            if ("RNDRUSDT".equals(candlestick2.getSymbol())) {
//                System.out.println("RNDRUSDT");
//            }


            if (
                //特征1：底上移、顶上移、收盘上移（同方向）------------回避较长上引线、第二条K线为最佳
                    //连涨的趋势或反转机会
                    (candlestick3.getClose().compareTo(candlestick2.getClose()) > 0             //收盘新高
//                            && candlestick3.getClose().divide(candlestick2.getClose(),4, RoundingMode.HALF_UP).compareTo(new BigDecimal(1.004)) > 0 // 有一定的涨幅
                            && candlestick3.getLow().compareTo(candlestick2.getLow()) > 0       //底上移
                            && candlestick3.getHigh().compareTo(candlestick2.getHigh()) > 0     //顶新高
                            && candlestick2.getClose().compareTo(candlestick2.getOpen()) > 0        //同方向
//                            && candlestick1.getClose().compareTo(candlestick1.getOpen()) < 0        //只筛选第二条策略

                    )

                    || (candlestick3.getClose().compareTo(candlestick2.getClose()) < 0
                            && candlestick3.getLow().compareTo(candlestick2.getLow()) < 0
//                            && candlestick2.getClose().divide(candlestick3.getClose(),4, RoundingMode.HALF_UP).compareTo(new BigDecimal(1.004)) > 0 // 有一定的涨幅
                            && candlestick3.getHigh().compareTo(candlestick2.getHigh()) < 0
                            && candlestick2.getClose().compareTo(candlestick2.getOpen()) < 0        //同方向        //同方向
//                            && candlestick1.getClose().compareTo(candlestick1.getOpen()) > 0
                    )


                //特征2：底上移、顶上移、收盘上移（同方向）------------回避较长上引线、第二条K线为最佳


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
