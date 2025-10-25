//package com.digcoo.fitech.quant.observer;
//
//import com.digcoo.fitech.common.config.Config;
//import com.digcoo.fitech.common.dto.Position;
//import com.digcoo.fitech.common.dto.Trade;
//import com.digcoo.fitech.quant.core.PortfolioModule;
//import com.digcoo.fitech.quant.observer.base.TradeObserver;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
///**
// * 1、止损订单触发 -> 止损订单成交执行
// * 2、
// *
// *
// */
//@Slf4j
//@Component
//public class PriceMonitorObserver implements TradeObserver {
//    private final PortfolioModule portfolio;
//    private final Config config;
//
//    public PriceMonitorObserver(PortfolioModule portfolio, Config config) {
//        this.portfolio = portfolio;
//        this.config = config;
//    }
//
//    @Override
//    public void onTradeUpdate(String symbol, Trade trade) {
//        Position position = portfolio.getPositionOrInit(symbol);
//        if (position == null || position.isFlat()) {
//            return;
//        }
//
////        double currentPrice = ticker.getLastPrice().doubleValue();
////        double entryPrice = position.getEntryPrice().doubleValue();
////        double priceChange = (currentPrice - entryPrice) / entryPrice * 100;
//
////        if (Math.abs(priceChange) >= priceChangeThreshold) {
////            System.out.printf("Price alert: %s has changed %.2f%% from entry price%n",
////                    symbol, priceChange);
////            // 这里可以触发风控操作
////        }
//    }
//}