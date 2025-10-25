package com.digcoo.fitech.backtest.observer;//package com.digcoo.fitech.quant.observer;
//
//import com.digcoo.fitech.common.dto.Candlestick;
//import com.digcoo.fitech.quant.core.OrderExecutionModule;
//import com.digcoo.fitech.quant.core.PortfolioModule;
//import com.digcoo.fitech.quant.observer.base.CandlestickObserver;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//
///**
// *  价格保护信号触发 -> 撤销所有委托
// *                -> 市价平仓
// *
// */
//@Slf4j
//@Component
//public class RiskManagementObserver implements CandlestickObserver {
//    private final PortfolioModule portfolio;
//    private final OrderExecutionModule orderExecutor;
//
//    public RiskManagementObserver(PortfolioModule portfolio,
//                                  OrderExecutionModule orderExecutor) {
//        this.portfolio = portfolio;
//        this.orderExecutor = orderExecutor;
//    }
//
//    @Override
//    public void onCandleUpdate(String symbol, CandlestickPeriod period, List<Candlestick> candlesticks) {
//        Candlestick candlestick0 = candlesticks.get(candlesticks.size() - 1);
//
//    }
//
//}
