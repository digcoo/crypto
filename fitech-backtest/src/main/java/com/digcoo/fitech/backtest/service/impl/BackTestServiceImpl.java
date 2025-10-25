package com.digcoo.fitech.backtest.service.impl;

import com.digcoo.fitech.backtest.core.*;
import com.digcoo.fitech.backtest.param.req.BackTestReqParam;
import com.digcoo.fitech.backtest.param.res.BackTestResult;
import com.digcoo.fitech.backtest.service.BackTestService;
import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.model.Position;
import com.digcoo.fitech.common.model.Signal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BackTestServiceImpl implements BackTestService {

    private final DataFeedModule dataFeed;
    private final OrderExecutionModule orderExecutor;
    private final Config config;
    private final PortfolioModule portfolio;
    private final RiskManagementModule riskManager;
    private final PriceProtectionModule priceProtector;

    public BackTestServiceImpl(DataFeedModule dataFeedModule
            , OrderExecutionModule orderExecutor
            , RiskManagementModule riskManager
            , PriceProtectionModule priceProtector
            , PortfolioModule portfolio
            , Config config) {
        this.dataFeed = dataFeedModule;
        this.orderExecutor = orderExecutor;
        this.riskManager = riskManager;
        this.priceProtector = priceProtector;
        this.portfolio = portfolio;
        this.config = config;
    }


    @Override
    public BackTestResult runBackTest(BackTestReqParam param) {

        //1、 查询全量历史数据
        List<Candlestick> allHistoricalCandlesticks = dataFeed.queryHistoryData(param.getSymbol(), config.getPeriod());

        //2、 预热历史K线
        List<Candlestick> warmupHistoricalCandlesticks = allHistoricalCandlesticks.subList(0, config.getWarmupPeriod());
        dataFeed.loadHistoryData(param.getSymbol(), config.getPeriod(), warmupHistoricalCandlesticks);


        //3、 模拟逐根K线回测
        for (int i = config.getWarmupPeriod(); i < allHistoricalCandlesticks.size(); i++) {
            Candlestick realtimeCandlestick = allHistoricalCandlesticks.get(i);

//            log.info("{} - {}", realtimeCandlestick.getOpenTimeStr(), realtimeCandlestick.getClose());

            dataFeed.updateRealtimeCandlestick(param.getSymbol(), config.getPeriod(), realtimeCandlestick);

//            // 获取当前仓位
//            Position position = portfolio.getPositionOrInit(config.getSymbol());
//
//            // 生成交易信号
//            Signal signal = strategy.generateSignal(subList, position);
//
//            // 执行信号
//            if (signal != Signal.NO_SIGNAL) {
//                executeSignal(signal, position);
//            }
//
//            // 监控价格变化
//            priceProtector.monitorPrice(config.getSymbol(), subList);
//
//            // 更新仓位和账户信息
//            portfolio.update(ImmutableMap.of(config.getSymbol(), historicalCandlesticks.get(i).getClose()));
        }
//        // 生成回测报告
//        BackTestResult result = statistics.generateReport();
//        statistics.printReport(result);

            return BackTestResult.builder().build();
        }
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

}
