package com.digcoo.fitech.backtest.core;

import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.common.model.Position;
import com.digcoo.fitech.common.model.Trade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class PortfolioModule {
    private BigDecimal initialBalance;  // 初始资金
    private BigDecimal currentBalance; // 可用资金
    private BigDecimal totalAssetValue; // 总资产价值(资金+持仓市值)

    private final Map<String, Position> positions = new ConcurrentHashMap<>();
    private final List<Trade> tradeHistory = new ArrayList<>();

    // 用于计算绩效指标
    private BigDecimal peakValue;      // 资产峰值
    private BigDecimal troughValue;   // 资产谷值
    private BigDecimal maxDrawDown;    // 最大回撤

    private final Config config;

    public PortfolioModule(Config config) {
        this.config = config;
        this.initialBalance = config.getInitialBalance();
        this.currentBalance = initialBalance;
        this.totalAssetValue = initialBalance;
        this.peakValue = initialBalance;
        this.troughValue = initialBalance;
        this.maxDrawDown = BigDecimal.ZERO;
    }

    /**
     * 更新持仓市值和总资产价值
     * @param currentPrices 当前各币种价格
     */
    public void update(Map<String, BigDecimal> currentPrices) {
        BigDecimal positionsValue = BigDecimal.ZERO;

        // 计算所有持仓的当前市值
        for (Map.Entry<String, Position> entry : positions.entrySet()) {
            String symbol = entry.getKey();
            Position position = entry.getValue();

            if (currentPrices.containsKey(symbol)) {
                BigDecimal currentPrice = currentPrices.get(symbol);
                position.updateMarketValue(currentPrice);
                positionsValue = positionsValue.add(position.getMarketValue());
            }
        }

        // 更新总资产价值
        this.totalAssetValue = currentBalance.add(positionsValue);

        // 更新最大回撤
        updateDrawDownStatistics();
    }

    /**
     * 更新单个币种价格
     */
    public void update(String symbol, BigDecimal currentPrice) {
        Map<String, BigDecimal> prices = new HashMap<>();
        prices.put(symbol, currentPrice);
        update(prices);
    }

    /**
     * 执行交易后的更新
     */
    public void executeTrade(Trade trade) {
        String symbol = trade.getSymbol();
        BigDecimal price = trade.getPrice();
        BigDecimal quantity = trade.getQty();
        BigDecimal fee = trade.getFee();

        // 计算交易成本
        BigDecimal cost = price.multiply(quantity).add(fee);

        if (trade.isOpenDeal()) {
            // 扣除资金
            currentBalance = currentBalance.subtract(cost);

            // 更新或创建仓位
            if (positions.containsKey(symbol)) {
                Position position = positions.get(symbol);
                position.addToPosition(quantity, price, fee);
            } else {
                Position newPosition = new Position(symbol, quantity, price, fee);
                positions.put(symbol, newPosition);
            }
        } else {
            // 增加资金
            currentBalance = currentBalance.add(price.multiply(quantity).subtract(fee));

            // 减少或清除仓位
            if (positions.containsKey(symbol)) {
                Position position = positions.get(symbol);
                position.reducePosition(quantity, price, fee);

                // 如果仓位清零，移除记录
                if (position.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                    positions.remove(symbol);
                }
            }
        }

        // 记录交易历史
        tradeHistory.add(trade);

        // 更新总资产价值
        update(trade.getSymbol(), trade.getPrice());
    }

    /**
     * 获取指定币种的持仓
     */
    public Position getPositionOrInit(String symbol) {
        Position position = positions.get(symbol);
        if (Objects.nonNull(position)) {
            return position;
        }
        return Position.emptyPosition(symbol);
    }

    /**
     * 获取所有持仓
     */
    public Map<String, Position> getPositions() {
        return new HashMap<>(positions);
    }

    /**
     * 获取交易历史
     */
    public List<Trade> getTradeHistory() {
        return new ArrayList<>(tradeHistory);
    }


    /**
     * 计算总收益率
     */
    public BigDecimal getTotalReturn() {
        return totalAssetValue.subtract(initialBalance)
                .divide(initialBalance, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
    }

    /**
     * 更新回撤统计
     */
    private void updateDrawDownStatistics() {
        // 更新峰值
        if (totalAssetValue.compareTo(peakValue) > 0) {
            peakValue = totalAssetValue;
            troughValue = totalAssetValue;
        }
        // 更新谷值
        else if (totalAssetValue.compareTo(troughValue) < 0) {
            troughValue = totalAssetValue;

            // 计算回撤
            BigDecimal drawdown = peakValue.subtract(troughValue)
                    .divide(peakValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));

            // 更新最大回撤
            if (drawdown.compareTo(maxDrawDown) > 0) {
                maxDrawDown = drawdown;
            }
        }
    }

    /**
     * 获取当前持仓的简要信息
     */
    public String getPortfolioSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Portfolio Summary:%n"));
        sb.append(String.format("Initial Balance: %s USDT%n", initialBalance));
        sb.append(String.format("Current Balance: %s USDT%n", currentBalance));
        sb.append(String.format("Total Asset Value: %s USDT%n", totalAssetValue));
        sb.append(String.format("Total Return: %.2f%%%n", getTotalReturn()));
        sb.append(String.format("Max DrawDown: %.2f%%%n", maxDrawDown));

        if (!positions.isEmpty()) {
            sb.append(String.format("%nCurrent Positions:%n"));
            positions.forEach((symbol, position) -> {
                sb.append(String.format("- %s: %s @ %s (Market Value: %s USDT)%n",
                        symbol,
                        position.getQuantity(),
                        position.getEntryPrice(),
                        position.getMarketValue()));
            });
        }

        return sb.toString();
    }
}
