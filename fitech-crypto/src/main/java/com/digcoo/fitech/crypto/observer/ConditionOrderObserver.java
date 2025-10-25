//package com.digcoo.fitech.quant.observer;
//
//import com.digcoo.fitech.common.config.Config;
//import com.digcoo.fitech.common.dto.*;
//import com.digcoo.fitech.common.enums.OrderSide;
//import com.digcoo.fitech.common.util.INoGenerator;
//import com.digcoo.fitech.quant.core.OrderExecutionModule;
//import com.digcoo.fitech.quant.core.PortfolioModule;
//import com.digcoo.fitech.quant.observer.base.CandlestickObserver;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
///**
// * 1、条件单触发 -> 止损订单成交执行
// * 2、
// *
// *
// */
//@Slf4j
//@Component
//public class ConditionOrderObserver implements CandlestickObserver {
//    private final PortfolioModule portfolio;
//    private final Config config;
//    private final OrderExecutionModule orderExecutionModule;
//    private final INoGenerator noGenerator;
//
//    public ConditionOrderObserver(PortfolioModule portfolio, Config config, OrderExecutionModule orderExecutionModule, INoGenerator noGenerator) {
//        this.portfolio = portfolio;
//        this.config = config;
//        this.orderExecutionModule = orderExecutionModule;
//        this.noGenerator = noGenerator;
//    }
//
//    @Override
//    public void onCandleUpdate(String symbol, CandlestickPeriod period, List<Candlestick> candlesticks) {
//        Position position = portfolio.getPositionOrInit(symbol);
//        if (position == null || position.isFlat()) {
//            return;
//        }
//
//        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
//        List<Order> toConditionOrders = queryConditionOrders(candlestick0);
//
//        if (CollectionUtils.isEmpty(toConditionOrders)) {
//            return;
//        }
//
//        //处理条件单
//        processConditionOrders(toConditionOrders, candlestick0);
//
//    }
//
//
//
//    /**
//     * 查询当前周期触发的成交单
//     * @param realtimeCandlestick
//     * @return
//     */
//    private List<Order> queryConditionOrders(Candlestick realtimeCandlestick) {
//        List<Order> orders = orderExecutionModule.queryOrders(realtimeCandlestick.getSymbol(), true);
//        List<Order> dealOrders = new ArrayList<>(orders.size());
//        if (CollectionUtils.isEmpty(orders)) {
//            return Collections.emptyList();
//        }
//
//        for (Order order : orders) {
//            if(order.getOrderSide() == OrderSide.BUY) {
//                if (realtimeCandlestick.getLow().compareTo(order.getPrice()) < 0) {
//                    dealOrders.add(order);
//                }
//            } else if (order.getOrderSide() == OrderSide.SELL) {
//                if (realtimeCandlestick.getHigh().compareTo(order.getPrice()) > 0) {
//                    dealOrders.add(order);
//                }
//            }
//        }
//        return dealOrders;
//    }
//
//
//    private void processConditionOrders(List<Order> toTriggerOrders, Candlestick candlestick) {
//        for (Order triggerOrder : toTriggerOrders) {
//            Trade trade = buildTrade(triggerOrder, candlestick, false);
//            processSlOrder(trade, triggerOrder);
//        }
//    }
//
//    private void processSlOrder(Trade trade, Order slOrder) {
//
//        //更新订单
//        orderExecutionModule.makeDealOrder(slOrder, trade);
//
//        //更新仓位
//        portfolio.executeTrade(trade);
//
//        log.info("止损订单成交: ({}) ({}), 成交价格: {}-{} {}"
//                , trade.getTradeTimeStr()
//                , trade.getSymbol()
//                , trade.getPrice()
//                , trade.getOrderSide()
//                , slOrder.getOrderId());
//    }
//
//    private Trade buildTrade(Order order, Candlestick candlestick, boolean openDeal) {
//        return Trade.builder()
//                .tradeId(String.valueOf(noGenerator.nextNo()))
//                .symbol(order.getSymbol())
//                .orderSide(order.getOrderSide())
//                .order(order)
//                .price(order.getPrice())
//                .qty(order.getQty())
//                .fee(order.getPrice().multiply(order.getQty()).multiply(config.getTakerFeeRate()))
//                .openDeal(openDeal)
//                .tradeTime(candlestick.getOpenTime())
//                .build();
//    }
//
//}