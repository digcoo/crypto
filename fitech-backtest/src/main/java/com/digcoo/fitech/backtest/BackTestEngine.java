//package com.digcoo.fitech.backtest;
//
//
//import com.binance.connector.futures.client.FuturesClient;
//import com.binance.connector.futures.client.WebsocketClient;
//import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
//import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;
//import com.digcoo.fitech.common.config.Config;
//import com.digcoo.fitech.common.constants.GlobalConstants;
//import com.digcoo.fitech.common.model.Candlestick;
//import com.digcoo.fitech.common.model.Position;
//import com.digcoo.fitech.common.model.Signal;
//import com.digcoo.fitech.common.param.BackTestResult;
//import com.digcoo.fitech.common.strategy.FanBaoStrategy;
//import com.digcoo.fitech.common.strategy.base.TradeStrategy;
//import com.digcoo.fitech.common.util.INoGenerator;
//import com.digcoo.fitech.common.util.SerialNoGenerator;
//import com.digcoo.fitech.backtest.core.*;
//import com.digcoo.fitech.backtest.observer.ConditionOrderObserver;
//import com.digcoo.fitech.backtest.observer.MatcherHandlerObserver;
//import com.digcoo.fitech.backtest.observer.StrategySignalObserver;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Slf4j
//public class BackTestEngine {
//    private DataFeedModule dataFeed;
//    private TradeStrategy strategy;
//    private OrderExecutionModule orderExecutor;
//    private RiskManagementModule riskManager;
//    private PriceProtectionModule priceProtector;
//    private BackTestStatisticsModule statistics;
//
//    private PortfolioModule portfolio;
//    private Config config;
//
//    private INoGenerator noGenerator;
//
//    public BackTestEngine(Config config) {
//        this.config = config;
//        initializeModules();
//    }
//
//    private void initializeModules() {
//        // 初始化所有模块
//        FuturesClient restClient = new UMFuturesClientImpl(GlobalConstants.API_KEY, GlobalConstants.SECRET_KEY);
//        WebsocketClient wsClient = new UMWebsocketClientImpl();
//        this.noGenerator = new SerialNoGenerator(1);
//        this.portfolio = new PortfolioModule(config);
//        this.strategy = new FanBaoStrategy(config); // 示例策略
//        this.orderExecutor = new OrderExecutionModule(restClient, config, noGenerator);
//        this.riskManager = new RiskManagementModule(orderExecutor, config);
//        this.priceProtector = new PriceProtectionModule(portfolio, orderExecutor, config);
//        this.dataFeed = new DataFeedModule(restClient, wsClient, config);
//        this.statistics = new BackTestStatisticsModule(portfolio);
//    }
//
//
//    public void run() {
//
//        //1、 查询全量历史数据
//        List<Candlestick> allHistoricalCandlesticks = dataFeed.queryHistoryData(config.getSymbol(), config.getPeriod());
//
//        //2、 预热历史K线
//        List<Candlestick> warmupHistoricalCandlesticks = allHistoricalCandlesticks.subList(0, config.getWarmupPeriod());
//        dataFeed.loadHistoryData(config.getSymbol(), config.getPeriod(), warmupHistoricalCandlesticks);
//
//        //3、 模拟逐根K线回测
//        for (int i = config.getWarmupPeriod(); i < allHistoricalCandlesticks.size(); i++) {
//            Candlestick realtimeCandlestick = allHistoricalCandlesticks.get(i);
//
////            log.info("{} - {}", realtimeCandlestick.getOpenTimeStr(), realtimeCandlestick.getClose());
//
//            dataFeed.updateRealtimeCandlestick(config.getSymbol(), config.getPeriod(), realtimeCandlestick);
//
////            // 获取当前仓位
////            Position position = portfolio.getPositionOrInit(config.getSymbol());
////
////            // 生成交易信号
////            Signal signal = strategy.generateSignal(subList, position);
////
////            // 执行信号
////            if (signal != Signal.NO_SIGNAL) {
////                executeSignal(signal, position);
////            }
////
////            // 监控价格变化
////            priceProtector.monitorPrice(config.getSymbol(), subList);
////
////            // 更新仓位和账户信息
////            portfolio.update(ImmutableMap.of(config.getSymbol(), historicalCandlesticks.get(i).getClose()));
//        }
//
//        // 生成回测报告
//        BackTestResult result = statistics.generateReport();
//        statistics.printReport(result);
//    }
//
//    private void executeSignal(Signal signal, Position position) {
//        log.info("{} signal trigger, {}-{}, {}, 委托价格: {}, 当前价格: {}"
//                , strategy.getStrategyType()
//                , signal.getSymbol(), signal.getOrderSide(), signal.getTimestampStr()
//                , signal.getDelegatePrice(), signal.getCurrentPrice());
//        // 这里可以调用orderExecutor下单
//
//        //TODO 委托下单
//    }
//
//}
