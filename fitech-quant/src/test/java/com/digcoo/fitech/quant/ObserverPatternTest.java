//package com.digcoo.fitech.quant;
//
//import com.binance.connector.futures.client.FuturesClient;
//import com.binance.connector.futures.client.WebsocketClient;
//import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
//import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;
//import com.digcoo.fitech.common.config.Config;
//import com.digcoo.fitech.common.constants.GlobalConstants;
//import com.digcoo.fitech.common.enums.CandlestickPeriod;
//import com.digcoo.fitech.common.strategy.FanBaoStrategy;
//import com.digcoo.fitech.common.strategy.base.TradeStrategy;
//import com.digcoo.fitech.common.util.INoGenerator;
//import com.digcoo.fitech.common.util.SerialNoGenerator;
//import com.digcoo.fitech.quant.core.PortfolioModule;
//import com.digcoo.fitech.quant.core.RealTimeDataHandler;
//import com.digcoo.fitech.quant.core.OrderExecutionModule;
//import com.digcoo.fitech.quant.observer.PriceMonitorObserver;
//import com.digcoo.fitech.quant.observer.StrategySignalObserver;
//import com.digcoo.fitech.quant.observer.base.CandlestickObserver;
//import com.digcoo.fitech.quant.observer.base.TradeObserver;
//
//import java.util.Arrays;
//
//public class ObserverPatternTest {
//    public static void main(String[] args) {
//        // 初始化组件
//        FuturesClient restClient = new UMFuturesClientImpl(GlobalConstants.API_KEY, GlobalConstants.SECRET_KEY);
//        WebsocketClient wsClient = new UMWebsocketClientImpl();
//        INoGenerator noGenerator = new SerialNoGenerator(1);
//        RealTimeDataHandler dataHandler = new RealTimeDataHandler(wsClient);
//
//        Config config = Config.getDefaultConfig();
//        PortfolioModule portfolio = new PortfolioModule(config);
//        TradeStrategy fanBaoStrategy = new FanBaoStrategy(config);
//        OrderExecutionModule orderExecutor = new OrderExecutionModule(restClient, config, noGenerator);
//
//        // 创建观察者
//        CandlestickObserver strategyObserver = new StrategySignalObserver(portfolio, orderExecutor, config, Arrays.asList(fanBaoStrategy));
//        TradeObserver priceMonitor = new PriceMonitorObserver(portfolio, config);
//
//        // 注册观察者
//        String symbol = "BTCUSDT";
//        CandlestickPeriod period = CandlestickPeriod.ONE_MINUTE;
//
//        dataHandler.addCandleObserver(strategyObserver);
////        dataHandler.addTickerObserver(symbol, priceMonitor);
//
//        // 订阅数据流
//        dataHandler.subscribeCandlestick(symbol, period);
////        dataHandler.subscribeTicker24H(symbol);
//
//        // 保持运行一段时间
//        try {
//            Thread.sleep(300000); // 运行5分钟
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        // 取消订阅和移除观察者
//        dataHandler.unsubscribe(symbol + "@kline_" + period);
//        dataHandler.unsubscribe(symbol + "@ticker");
//        dataHandler.unsubscribe(symbol + "@depth20@100ms");
//
//        dataHandler.removeCandleObserver(strategyObserver);
////        dataHandler.removeTickerObserver(symbol, priceMonitor);
//    }
//}
