package com.digcoo.fitech.quant.observer;

import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.model.Position;
import com.digcoo.fitech.common.model.Signal;
import com.digcoo.fitech.common.enums.StrategyType;
import com.digcoo.fitech.common.param.DelegateOrderParam;
import com.digcoo.fitech.common.param.OrderResult;
import com.digcoo.fitech.common.strategy.base.TradeStrategy;
import com.digcoo.fitech.quant.core.OrderExecutionModule;
import com.digcoo.fitech.quant.core.PortfolioModule;
import com.digcoo.fitech.quant.observer.base.CandlestickObserver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 1、建仓信号触发 -> 撤销当前委托
 *               -> 新委托下单
 */
@Slf4j
@Component
public class StrategySignalObserver implements CandlestickObserver {

    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";

    private final PortfolioModule portfolio;
    private final OrderExecutionModule orderExecutor;

    private final Config config;

    private List<TradeStrategy> strategies;

    private final Map<StrategyType, TradeStrategy> strategyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (TradeStrategy tradeStrategy : strategies) {
            strategyMap.put(tradeStrategy.getStrategyType(), tradeStrategy);
        }
    }

    public StrategySignalObserver(PortfolioModule portfolio,
                                  OrderExecutionModule orderExecutor,
                                  Config config,
                                  List<TradeStrategy> strategies) {
        this.portfolio = portfolio;
        this.orderExecutor = orderExecutor;
        this.config = config;
        this.strategies = strategies;
    }

    @Override
    public void onCandleUpdate(String symbol, CandlestickPeriod period, List<Candlestick> candlesticks) {
        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);

        // 获取当前仓位
        Position position = portfolio.getPositionOrInit(symbol);

        // 生成交易信号
        for (TradeStrategy strategy : strategies) {

            Signal signal = strategy.generateSignal(symbol, period, candlesticks, position);

            if (Signal.NO_SIGNAL == signal) {
//                log.info("({}) {}({}-{}) no signal"
//                        , candlestick0.getOpenTimeStr(), strategy.getStrategyType(), symbol, period);
                continue;
            }

            // 执行信号
            executeSignal(signal, symbol);

        }

    }

    private void executeSignal(Signal signal, String symbol) {

        DelegateOrderParam delegateOrderParam = DelegateOrderParam.builder()
                .symbol(symbol)
                .orderSide(signal.getOrderSide())
                .qty(signal.getDelegateQty())
                .delegatePrice(signal.getDelegatePrice())
                .tpPrice(signal.getTpPrice())
                .slPrice(signal.getSlPrice())
                .build();

        OrderResult orderResult = this.orderExecutor.openLimitOrder(delegateOrderParam);

        if (!orderResult.isSuccess()) {
//            log.warn("({}) {}({}-{}) signal trigger, 委托下单失败"
//                    , signal.getTimestampStr()
//                    , signal.getStrategyType()
//                    , symbol
//                    , signal.getPeriod());
            return;
        }

        log.info("({}) {}({}-{}) signal trigger, 委托价格: {}-{}, 当前价格: {}, {}, {}"
                , signal.getTimestampStr(), signal.getStrategyType()
                , symbol, signal.getPeriod()
                , signal.getDelegatePrice(), signal.getOrderSide()
                , signal.getCurrentPrice(), orderResult.getOrder().getOrderId()
                , link_pref + symbol
        );

    }
}
