package com.digcoo.fitech.backtest;

import com.digcoo.fitech.backtest.core.PortfolioModule;
import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.common.enums.OrderSide;
import com.digcoo.fitech.common.model.Trade;

import java.math.BigDecimal;

public class PortfolioTest {
    public static void main(String[] args) {
        // 初始化投资组合(初始资金10000 USDT)
        PortfolioModule portfolio = new PortfolioModule(Config.getDefaultConfig());

        // 假设BTC当前价格
        BigDecimal btcPrice = new BigDecimal("50000");

        // 执行买入交易(买入0.2 BTC)
        Trade buyTrade = Trade.builder()
                .symbol("BTCUSDT")
                .side(OrderSide.BUY)
                .price(btcPrice)
                .qty(new BigDecimal("0.2"))
                .fee(new BigDecimal("10"))  // 手续费10 USDT
                .feeAsset("USDT")
                .build();
        portfolio.executeTrade(buyTrade);

        // 更新市场价格(BTC涨到52000)
        portfolio.update("BTCUSDT", new BigDecimal("52000"));

        // 打印投资组合摘要
        System.out.println(portfolio.getPortfolioSummary());

        // 执行卖出交易(卖出0.1 BTC)
        Trade sellTrade = Trade.builder()
                .symbol("BTCUSDT")
                .side(OrderSide.SELL)
                .price(new BigDecimal("52000"))
                .qty(new BigDecimal("0.1"))
                .fee(new BigDecimal("5.20"))  // 手续费5.20 USDT
                .feeAsset("USDT")
                .build();
        portfolio.executeTrade(sellTrade);

        // 再次更新市场价格(BTC跌到51000)
        portfolio.update("BTCUSDT", new BigDecimal("51000"));

        // 打印最终投资组合摘要
        System.out.println(portfolio.getPortfolioSummary());

        // 打印交易历史
        System.out.println("\nTrade History:");
        portfolio.getTradeHistory().forEach(trade -> {
            System.out.printf("%s %s %s @ %s (Fee: %s %s)%n",
                    trade.getSide(),
                    trade.getQty(),
                    trade.getSymbol(),
                    trade.getPrice(),
                    trade.getFee(),
                    trade.getFeeAsset());
        });
    }
}
