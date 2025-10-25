package com.digcoo.fitech.backtest.param.res;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BackTestResult {
    int winTrades = 0;
    int loseTrades = 0;
    int totalTrades = 0;
    BigDecimal winRate = BigDecimal.ZERO;
    BigDecimal totalProfit = BigDecimal.ZERO;
    BigDecimal maxDrawDown = BigDecimal.ZERO;
    BigDecimal profitFactor = BigDecimal.ZERO;
    BigDecimal finalBalance = BigDecimal.ZERO;
}