package com.digcoo.fitech.backtest.core;


import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.model.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
public class PriceProtectionModule {
    private final PortfolioModule portfolio;
    private final OrderExecutionModule orderExecutionModule;
    private final Config config;

    public PriceProtectionModule(PortfolioModule portfolio, OrderExecutionModule orderExecutionModule, Config config) {
        this.portfolio = portfolio;
        this.orderExecutionModule = orderExecutionModule;
        this.config = config;
    }

    public void monitorPrice(String symbol, List<Candlestick> candles) {
        // 监控价格变化
        Position position = portfolio.getPositionOrInit(symbol);
        if (position == null || position.isFlat()) {
            return;
        }

        BigDecimal currentPrice = candles.get(candles.size()-1).getClose();
        BigDecimal entryPrice = position.getEntryPrice();
        BigDecimal changePercent = currentPrice.subtract(entryPrice)
                .divide(entryPrice, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        // 价格快速下跌保护
        if (isSharpDrop(candles)) {
            triggerProtectionMechanism(position, "Sharp price drop detected");
        }

        // 波动率增加保护
        if (isHighVolatility(candles)) {
            triggerProtectionMechanism(position, "High volatility detected");
        }
    }

    private boolean isSharpDrop(List<Candlestick> candles) {
        // 检测价格快速下跌逻辑
        // 实现您的策略
        return false;
    }

    private boolean isHighVolatility(List<Candlestick> candles) {
        // 检测高波动率逻辑
        // 实现您的策略
        return false;
    }

    private void triggerProtectionMechanism(Position position, String reason) {
        // 触发保护机制，如提前平仓
        log.info("Price protection triggered: {}", reason);
        // 这里可以调用订单模块平仓
    }
}
