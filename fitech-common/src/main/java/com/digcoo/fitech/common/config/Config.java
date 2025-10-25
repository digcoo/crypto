package com.digcoo.fitech.common.config;

import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.enums.StrategyType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Config {

//    private String symbol;
    private CandlestickPeriod period;
    private int dataLimit;
    private BigDecimal initialBalance;
    private BigDecimal riskPerTrade;
    private BigDecimal takeProfitPercent;
    private BigDecimal stopLossPercent;
    private BigDecimal takerFeeRate;
    private BigDecimal makerFeeRate;

    //预热周期数
    private int warmupPeriod;
    private boolean backTest;
    private StrategyType strategyType;
    private int topKSymbolCount = 50;

    //FAN_BAO策略参数
    private BigDecimal fanbaoPricePercent;       //委托价格：相比前一根K线的实体比率
    private BigDecimal fanbaoProfitPercent;       //预期盈利区间：相比前一根K线的空间比率
    private BigDecimal fanbaoMaxProfitPercent;       //预期盈利区间：最大盈利区间比率
    private BigDecimal fanbaoMinProfitPercent;       //保底盈利区间

    private long spiderIntervalMilSeconds = 1000;    // 反爬策略

    public static Config getDefaultConfig() {
        Config config = new Config();
//        config.setSymbol("BTCUSDT");
        config.setPeriod(CandlestickPeriod.HALF_HOURLY);
        config.setDataLimit(300);
        config.setInitialBalance(new BigDecimal(10000));
        config.setRiskPerTrade(new BigDecimal(1.0));
        config.setTakeProfitPercent(new BigDecimal(5.0));
        config.setStopLossPercent(new BigDecimal(2.0));
        config.setWarmupPeriod(50);
        config.setTakerFeeRate(new BigDecimal(0.0006));
        config.setMakerFeeRate(new BigDecimal(0.0006));
        config.setBackTest(true);
        config.setStrategyType(StrategyType.FAN_BAO);

        //反包策略参数
        config.setFanbaoPricePercent(new BigDecimal(0.5));
        config.setFanbaoProfitPercent(new BigDecimal(1.5));
        config.setFanbaoMinProfitPercent(new BigDecimal(0.008));
        config.setFanbaoMaxProfitPercent(new BigDecimal(0.03));

        return config;
    }


    public static Config getStockConfig() {
        Config config = new Config();
//        config.setSymbol("BTCUSDT");
        config.setPeriod(CandlestickPeriod.DAILY);
        config.setDataLimit(500);
        config.setInitialBalance(new BigDecimal(10000));
        config.setRiskPerTrade(new BigDecimal(1.0));
        config.setTakeProfitPercent(new BigDecimal(5.0));
        config.setStopLossPercent(new BigDecimal(2.0));
        config.setWarmupPeriod(50);
        config.setTakerFeeRate(new BigDecimal(0.0006));
        config.setMakerFeeRate(new BigDecimal(0.0006));
        config.setBackTest(true);
        config.setStrategyType(StrategyType.FAN_BAO);

        //反包策略参数
        config.setFanbaoPricePercent(new BigDecimal(0.5));
        config.setFanbaoProfitPercent(new BigDecimal(1.5));
        config.setFanbaoMinProfitPercent(new BigDecimal(0.008));
        config.setFanbaoMaxProfitPercent(new BigDecimal(0.03));

        return config;
    }

}
