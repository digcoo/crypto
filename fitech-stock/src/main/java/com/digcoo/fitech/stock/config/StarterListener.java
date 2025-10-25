package com.digcoo.fitech.stock.config;

import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.common.constants.GlobalConstants;
import com.digcoo.fitech.stock.core.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;



@Slf4j
@Component
public class StarterListener implements ApplicationListener<ContextRefreshedEvent> {

    private final DataFeedModule dataFeed;
    private final OrderExecutionModule orderExecutor;
    private final PortfolioModule portfolio;
    private final RiskManagementModule riskManager;
    private final PriceProtectionModule priceProtector;
    private Config config;

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

        //预热
        dataFeed.warmUp();

        //启动
        dataFeed.start();

    }

}
