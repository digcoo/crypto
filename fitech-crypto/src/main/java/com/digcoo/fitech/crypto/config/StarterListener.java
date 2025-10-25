package com.digcoo.fitech.crypto.config;

import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.crypto.core.*;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class StarterListener implements ApplicationListener<ContextRefreshedEvent> {

    private final DataFeedModule dataFeed;
    private final OrderExecutionModule orderExecutor;
    private final Config config;
    private final PortfolioModule portfolio;
    private final RiskManagementModule riskManager;
    private final PriceProtectionModule priceProtector;

    public StarterListener(DataFeedModule dataFeedModule
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
    public void onApplicationEvent(ContextRefreshedEvent event) {

        List<String> symbols = dataFeed.getTopVolumeSymbols(config.getTopKSymbolCount());
        for (String symbol : symbols) {

            //初始化历史数据
            dataFeed.loadHistoryData(symbol, config.getPeriod());

            // 订阅实时数据
            dataFeed.subscribeMarketData(symbol, config.getPeriod());

        }

    }

}
