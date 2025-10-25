package com.digcoo.fitech.backtest.param.req;

import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.enums.StrategyType;
import com.digcoo.fitech.common.strategy.base.TradeStrategy;
import lombok.Data;

@Data
public class BackTestReqParam {
    private String symbol;
    private CandlestickPeriod period;
    private String startDate;
    private String endDate;
    private String strategyType;

    public StrategyType toStrategyType() {
        return StrategyType.valueOf(strategyType);
    }
}
