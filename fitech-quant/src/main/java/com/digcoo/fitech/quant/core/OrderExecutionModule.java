package com.digcoo.fitech.quant.core;


import com.binance.connector.futures.client.FuturesClient;
import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.common.model.Order;
import com.digcoo.fitech.common.model.Trade;
import com.digcoo.fitech.common.enums.OrderState;
import com.digcoo.fitech.common.enums.OrderType;
import com.digcoo.fitech.common.param.DelegateOrderParam;
import com.digcoo.fitech.common.param.OrderResult;
import com.digcoo.fitech.common.util.INoGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Slf4j
@Component
public class OrderExecutionModule {
    private final FuturesClient restClient;
    private final Config config;
    private final INoGenerator noGenerator;
    Map<String, Set<Order>> orderMap = new ConcurrentHashMap<>();

    public OrderExecutionModule(FuturesClient restClient, Config config, INoGenerator noGenerator) {
        this.restClient = restClient;
        this.config = config;
        this.noGenerator = noGenerator;
    }

    public List<Order> queryOrders(String symbol, boolean isConditionOrder) {
        Set<Order> orders = orderMap.get(symbol);
        if (CollectionUtils.isEmpty(orders)) {
            return Collections.emptyList();
        }
        return orders.stream()
                .filter(x -> x.getOrderType().isConditionOrder())
                .collect(Collectors.toList());
    }

    public OrderResult openMarketOrder(DelegateOrderParam delegateOrderParam) {
        String symbol = delegateOrderParam.getSymbol();
        Set<Order> orders = this.orderMap.get(symbol);
        if (!CollectionUtils.isEmpty(orders)) {
            log.warn("exist orders, can not open new order: {}", symbol);
            return new OrderResult(false, null);
        }

        Order order = Order.builder()
                .orderType(OrderType.MARKET)
                .isOpen(true)
                .orderId(String.valueOf(noGenerator.nextNo()))
                .symbol(symbol)
                .orderSide(delegateOrderParam.getOrderSide())
                .qty(delegateOrderParam.getQty())
                .price(delegateOrderParam.getDelegatePrice())
                .tpPrice(delegateOrderParam.getTpPrice())
                .slPrice(delegateOrderParam.getSlPrice())
                .status(OrderState.CREATED)
                .build();
        orderMap.computeIfAbsent(symbol, k -> ConcurrentHashMap.newKeySet()).add(order);
//        String s = this.restClient.account().newOrder(order.toMap());

        return new OrderResult(true, order);
    }

    public OrderResult openLimitOrder(DelegateOrderParam delegateOrderParam) {
        String symbol = delegateOrderParam.getSymbol();
        Set<Order> orders = this.orderMap.get(symbol);
        if (!CollectionUtils.isEmpty(orders)) {
//            log.warn("exist orders, can not open new order: {}", symbol);
            return new OrderResult(false, null);
        }

        Order order = Order.builder()
                .orderType(OrderType.LIMIT)
                .isOpen(true)
                .orderId(String.valueOf(noGenerator.nextNo()))
                .symbol(symbol)
                .orderSide(delegateOrderParam.getOrderSide())
                .qty(delegateOrderParam.getQty())
                .price(delegateOrderParam.getDelegatePrice())
                .tpPrice(delegateOrderParam.getTpPrice())
                .slPrice(delegateOrderParam.getSlPrice())
                .status(OrderState.CREATED)
                .build();
        orderMap.computeIfAbsent(symbol, k -> ConcurrentHashMap.newKeySet()).add(order);
//        String s = this.restClient.account().newOrder(order.toMap());

        return new OrderResult(true, order);
    }

    public OrderResult closeMarketOrder(DelegateOrderParam delegateOrderParam) {
        String symbol = delegateOrderParam.getSymbol();
        Order order = Order.builder()
                .orderType(OrderType.MARKET)
                .isOpen(false)
                .orderId(String.valueOf(noGenerator.nextNo()))
                .symbol(symbol)
                .orderSide(delegateOrderParam.getOrderSide())
                .qty(delegateOrderParam.getQty())
                .price(delegateOrderParam.getDelegatePrice())
                .status(OrderState.CREATED)
                .build();
        order.setStatus(OrderState.CREATED);
        orderMap.computeIfAbsent(symbol, k -> ConcurrentHashMap.newKeySet()).add(order);

        //TODO 下单
//        String s = this.restClient.account().newOrder(order.toMap());

        return new OrderResult(true, order);
    }

    public OrderResult closeLimitOrder(DelegateOrderParam delegateOrderParam) {
        String symbol = delegateOrderParam.getSymbol();
        Order order = Order.builder()
                .orderType(OrderType.LIMIT)
                .isOpen(false)
                .orderId(String.valueOf(noGenerator.nextNo()))
                .symbol(symbol)
                .orderSide(delegateOrderParam.getOrderSide())
                .qty(delegateOrderParam.getQty())
                .price(delegateOrderParam.getDelegatePrice())
                .status(OrderState.CREATED)
                .build();
        orderMap.computeIfAbsent(symbol, k -> ConcurrentHashMap.newKeySet()).add(order);
//        String s = this.restClient.account().newOrder(order.toMap());

        return new OrderResult(true, order);
    }

//    public OrderResult placeMarketOrder(String symbol, OrderSide orderSide, BigDecimal quantity, BigDecimal currentPrice) {
//        Order order = Order.builder()
//                .symbol(symbol)
//                .orderSide(orderSide)
//                .orderType(OrderType.MARKET)
//                .quantity(quantity)
//                .price(currentPrice)
//                .build();
//        order.setStatus(OrderState.CREATED);
//        orders.add(order);
//        return new OrderResult(true, order);
//    }
//
//    public OrderResult placeLimitOrder(String symbol, OrderSide orderSide, BigDecimal quantity, BigDecimal delegatePrice) {
//        Order order = Order.builder()
//                .symbol(symbol)
//                .orderSide(orderSide)
//                .orderType(OrderType.LIMIT)
//                .quantity(quantity)
//                .price(delegatePrice)
//                .build();
//        order.setStatus(OrderState.CREATED);
//        orders.add(order);
//        return new OrderResult(true, order);
//    }

    public OrderResult placeStopLossOrder(DelegateOrderParam delegateOrderParam) {
        String symbol = delegateOrderParam.getSymbol();
        Order order = Order.builder()
                .orderType(OrderType.STOP)
                .isOpen(false)
                .orderId(String.valueOf(noGenerator.nextNo()))
                .symbol(symbol)
                .orderSide(delegateOrderParam.getOrderSide())
                .qty(delegateOrderParam.getQty())
                .price(delegateOrderParam.getDelegatePrice())
                .status(OrderState.CREATED)
                .build();
        orderMap.computeIfAbsent(symbol, k -> ConcurrentHashMap.newKeySet()).add(order);
//        String s = this.restClient.account().newOrder(order.toMap());

        return new OrderResult(true, order);
    }

    public void makeDealOrder(Order order, Trade trade) {
        this.orderMap.get(order.getSymbol()).remove(order);
    }

    public void cancelOrder(Order order) {
        this.orderMap.get(order.getSymbol()).remove(order);
    }

    public void cancelAllOrders(String symbol) {
        this.orderMap.remove(symbol);
    }

}
