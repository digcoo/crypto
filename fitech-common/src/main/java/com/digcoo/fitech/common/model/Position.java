package com.digcoo.fitech.common.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {
    private String symbol;
    private BigDecimal quantity;       // 持仓数量
    private BigDecimal entryPrice;     // 平均入场价格
    private BigDecimal investedAmount; // 投入总金额
    private BigDecimal marketValue;    // 当前市值
    private BigDecimal realizedPnl;    // 已实现盈亏
    private BigDecimal unrealizedPnl;  // 未实现盈亏


    public static Position emptyPosition(String symbol) {
        return Position.builder()
                .symbol(symbol)
                .quantity(BigDecimal.ZERO)
                .entryPrice(BigDecimal.ZERO)
                .investedAmount(BigDecimal.ZERO)
                .marketValue(BigDecimal.ZERO)
                .realizedPnl(BigDecimal.ZERO)
                .unrealizedPnl(BigDecimal.ZERO)
                .build();

    }

    public Position(String symbol, BigDecimal quantity,
                    BigDecimal entryPrice, BigDecimal fee) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.entryPrice = entryPrice;
        this.investedAmount = entryPrice.multiply(quantity).add(fee);
        this.marketValue = BigDecimal.ZERO;
        this.realizedPnl = BigDecimal.ZERO;
        this.unrealizedPnl = BigDecimal.ZERO;
    }

    /**
     * 增加持仓
     */
    public void addToPosition(BigDecimal additionalQuantity,
                              BigDecimal price, BigDecimal fee) {
        BigDecimal newQuantity = this.quantity.add(additionalQuantity);
        BigDecimal newInvested = this.investedAmount.add(
                price.multiply(additionalQuantity).add(fee));

        this.entryPrice = newInvested.divide(newQuantity, 8, RoundingMode.HALF_UP);
        this.quantity = newQuantity;
        this.investedAmount = newInvested;
    }

    /**
     * 减少持仓
     */
    public void reducePosition(BigDecimal reduceQuantity,
                               BigDecimal price, BigDecimal fee) {
        if (reduceQuantity.compareTo(quantity) >= 0) {
            // 平仓
            BigDecimal pnl = price.multiply(quantity)
                    .subtract(entryPrice.multiply(quantity))
                    .subtract(fee);
            realizedPnl = realizedPnl.add(pnl);
            quantity = BigDecimal.ZERO;
            investedAmount = BigDecimal.ZERO;
            entryPrice = BigDecimal.ZERO;
        } else {
            // 部分减仓
            BigDecimal pnl = price.multiply(reduceQuantity)
                    .subtract(entryPrice.multiply(reduceQuantity))
                    .subtract(fee);
            realizedPnl = realizedPnl.add(pnl);
            quantity = quantity.subtract(reduceQuantity);
            investedAmount = investedAmount.subtract(
                    entryPrice.multiply(reduceQuantity));
        }
    }

    /**
     * 更新市值
     */
    public void updateMarketValue(BigDecimal currentPrice) {
        this.marketValue = currentPrice.multiply(quantity);
        this.unrealizedPnl = marketValue.subtract(entryPrice.multiply(quantity));
    }

    // 检查是否空仓
    public boolean isFlat() {
        return quantity.compareTo(BigDecimal.ZERO) == 0;
    }

    // 检查是否多头仓位
    public boolean isLong() {
        return quantity.compareTo(BigDecimal.ZERO) > 0;
    }
}
