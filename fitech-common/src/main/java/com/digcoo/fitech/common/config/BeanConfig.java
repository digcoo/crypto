package com.digcoo.fitech.common.config;


import com.binance.connector.futures.client.FuturesClient;
import com.binance.connector.futures.client.WebsocketClient;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.binance.connector.futures.client.impl.UMWebsocketClientImpl;
import com.digcoo.fitech.common.constants.GlobalConstants;
import com.digcoo.fitech.common.util.SerialNoGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class BeanConfig {

    @Bean
    public SerialNoGenerator serialNoGenerator() {
        return new SerialNoGenerator(1);
    }

    @Bean
    public Config coinConfig() {
        String env = System.getenv(GlobalConstants.CONFIG_STOCK_KEY);
        if (GlobalConstants.CONFIG_STOCK_VALUE.equals(env)) {
            return Config.getStockConfig();
        }
        return Config.getDefaultConfig();
    }


    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

    @Bean
    public FuturesClient futuresClient() {
        return new UMFuturesClientImpl(GlobalConstants.API_KEY, GlobalConstants.SECRET_KEY);
    }

    @Bean
    public WebsocketClient websocketClient() {
        return new UMWebsocketClientImpl();
    }

}
