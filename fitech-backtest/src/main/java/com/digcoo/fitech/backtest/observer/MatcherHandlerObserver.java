package com.digcoo.fitech.backtest.observer;//package com.digcoo.fitech.quant.observer;
//
//import com.digcoo.fitech.common.config.Config;
//import com.digcoo.fitech.common.dto.Candlestick;
//import com.digcoo.fitech.common.dto.Order;
//import com.digcoo.fitech.common.dto.Trade;
//import com.digcoo.fitech.common.enums.OrderSide;
//import com.digcoo.fitech.common.param.DelegateOrderParam;
//import com.digcoo.fitech.common.param.OrderResult;
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
//
///**
// * 1、建仓委托成交 -> 更新仓位
// *               -> 止盈止损下单
// * 2、平仓委托成交 -> 更新仓位
// *
// */
//@Slf4j
//@Component
//public class MatcherHandlerObserver implements CandlestickObserver {
//    private final PortfolioModule portfolio;
//    private final OrderExecutionModule orderExecutor;
//    private final INoGenerator noGenerator;
//    private final Config config;
//
//    public MatcherHandlerObserver(PortfolioModule portfolio, OrderExecutionModule orderExecutor, Config config, INoGenerator noGenerator) {
//        this.portfolio = portfolio;
//        this.orderExecutor = orderExecutor;
//        this.config = config;
//        this.noGenerator = noGenerator;
//    }
//
//
//    @Override
//    public void onCandleUpdate(String symbol, CandlestickPeriod period, List<Candlestick> candlesticks) {
//        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
//
//        //是否有成交订单
//        List<Order> toDealOrders = queryDealOrders(candlestick0);
//        if (CollectionUtils.isEmpty(toDealOrders)) {
//            return;
//        }
//
//        //处理成交订单
//        processDealOrders(toDealOrders, candlestick0);
//    }
//
//    /**
//     * 查询当前周期触发的成交单
//     * @param realtimeCandlestick
//     * @return
//     */
//    private List<Order> queryDealOrders(Candlestick realtimeCandlestick) {
//        List<Order> orders = orderExecutor.queryOrders(realtimeCandlestick.getSymbol(), false);
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
//    private void processDealOrders(List<Order> dealOrders, Candlestick candlestick) {
//        for (Order dealOrder : dealOrders) {
//            if (dealOrder.isOpen()) {
//                //构建成交明细
//                Trade trade = buildTrade(dealOrder, candlestick, true);
//                processOpenDealOrder(trade, dealOrder);
//            } else {
//                Trade trade = buildTrade(dealOrder, candlestick, false);
//                processCloseDealOrder(trade, dealOrder);
//            }
//        }
//    }
//
//    /**
//     * 开仓成交
//     * @param dealOrder
//     */
//    private void processOpenDealOrder(Trade trade, Order dealOrder) {
//        //更新订单
//        orderExecutor.makeDealOrder(dealOrder, trade);
//
//        //更新仓位
//        portfolio.executeTrade(trade);
//
//        log.info("开仓成交: ({}) ({}), 成交价格: {}-{} {}"
//                , trade.getTradeTimeStr()
//                , trade.getSymbol()
//                , trade.getPrice()
//                , trade.getOrderSide()
//                , dealOrder.getOrderId());
//
//        //止盈止损下单
//        setupTakeProfit(trade, dealOrder);
//        setupStopLoss(trade, dealOrder);
//    }
//
//    /**
//     * 平仓成交
//     * @param dealOrder
//     */
//    private void processCloseDealOrder(Trade trade, Order dealOrder) {
//
//        //更新订单
//        orderExecutor.makeDealOrder(dealOrder, trade);
//
//        //更新仓位
//        portfolio.executeTrade(trade);
//        log.info("平仓成交: ({}) ({}), 成交价格: {}-{} {}"
//                , trade.getTradeTimeStr()
//                , trade.getSymbol()
//                , trade.getPrice()
//                , trade.getOrderSide()
//                , dealOrder.getOrderId());
//
//        //撤销止盈止损单
//        orderExecutor.cancelAllOrders(dealOrder.getSymbol());
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
//
//    public void setupTakeProfit(Trade trade, Order order) {
//        // 设置止盈订单
//        DelegateOrderParam delegateOrderParam = DelegateOrderParam.builder()
//                .symbol(trade.getSymbol())
//                .orderSide(trade.getOrderSide().getOppositeSide())
//                .qty(trade.getQty())
//                .delegatePrice(order.getTpPrice())
//                .build();
//        OrderResult orderResult = orderExecutor.closeLimitOrder(delegateOrderParam);
//
//        log.info("止盈下单: ({}) ({}), 止盈价格: {}-{} {}"
//                , trade.getTradeTimeStr()
//                , trade.getSymbol()
//                , order.getTpPrice()
//                , trade.getOrderSide().getOppositeSide()
//                , orderResult.getOrder().getOrderId());
//    }
//
//    public void setupStopLoss(Trade trade, Order order) {
//        // 设置止损订单
//        DelegateOrderParam delegateOrderParam = DelegateOrderParam.builder()
//                .symbol(trade.getSymbol())
//                .orderSide(trade.getOrderSide().getOppositeSide())
//                .qty(trade.getQty())
//                .delegatePrice(order.getSlPrice())
//                .build();
//        OrderResult orderResult = orderExecutor.placeStopLossOrder(delegateOrderParam);
//
//        log.info("止损下单: ({}) ({}), 止损价格: {}-{} {}"
//                , trade.getTradeTimeStr()
//                , trade.getSymbol()
//                , order.getSlPrice()
//                , trade.getOrderSide().getOppositeSide()
//                , orderResult.getOrder().getOrderId());
//    }
//}
