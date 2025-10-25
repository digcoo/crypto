package com.binance.client.examples.predicate;

import com.binance.client.utils.PriceUtil;
import com.binance.client.model.market.Candlestick;

import java.math.BigDecimal;
import java.util.List;

public class LianZhangMapFunction {


    BigDecimal targetZhenfu = null;


    BigDecimal tradeAssetVolume = null;

    int limit = 5;


    public LianZhangMapFunction(BigDecimal zhenfu, BigDecimal tradeAssetVolume, int limit) {
        this.targetZhenfu = zhenfu;
        this.tradeAssetVolume = tradeAssetVolume;
        this.limit = limit;
    }

    public Candlestick apply(List<Candlestick> candlesticks) {
        try {

            candlesticks = candlesticks.subList(limit-5, limit);

            Candlestick candlestick1 = candlesticks.get(0);
            Candlestick candlestick2 = candlesticks.get(1);
            Candlestick candlestick3 = candlesticks.get(2);
            Candlestick candlestick4 = candlesticks.get(3);
            Candlestick candlestick5 = candlesticks.get(4);

//            if ("RNDRUSDT".equals(candlestick5.getSymbol())) {
//                System.out.println("RNDRUSDT");
//            }

            //压力区连涨
            if (1 == 1
                    && PriceUtil.isSameTrend(candlesticks)
            ) {
                return candlestick4;
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;

    }
}
