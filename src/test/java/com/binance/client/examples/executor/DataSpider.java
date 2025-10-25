package com.binance.client.examples.executor;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.ExchangeInformation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class DataSpider extends Thread {

    DataContainer dataContainer;

    DataSpider(DataContainer dataContainer) {
        this.dataContainer = dataContainer;
    }

    @Override
    public void run() {

        int limit = 100;  //取5条k数据

        try {

            RequestOptions options = new RequestOptions();
            SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                    options);

            //获取所有合约交易对
            ExchangeInformation exchangeInformation = syncRequestClient.getExchangeInformation();
            List<String> symbols = exchangeInformation.getSymbols().stream().filter(x -> x.getSymbol().endsWith("USDT")).map(x -> x.getSymbol()).collect(Collectors.toList());

            //定时拉取K线并判断
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {

                StopWatch stopWatch = new StopWatch();
                stopWatch.start();

                for (String symbol : symbols) {
                    try {

//                      hour4Candlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.HOUR4.getInterval(), null, null, limit);
                        List<Candlestick> dayCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.DAY.getInterval(), null, null, limit);
                        List<Candlestick> weekCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.WEEK.getInterval(), null, null, limit);
                        List<Candlestick> monthCandlesticks = syncRequestClient.getCandlestick(symbol, PeriodTypeEnum.MONTH.getInterval(), null, null, limit);

                        this.dataContainer.setDayCandlesticks(dayCandlesticks);
                        this.dataContainer.setWeekCandlesticks(weekCandlesticks);
                        this.dataContainer.setMonthCandlesticks(monthCandlesticks);

                        stopWatch.stop();
                        log.info("耗时[{}]秒", stopWatch.getTime() / 1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }, 0, 5, TimeUnit.MINUTES);

        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Data
    public class DataContainer {
        List<Candlestick> dayCandlesticks = null;
        List<Candlestick> weekCandlesticks = null;
        List<Candlestick> monthCandlesticks = null;
    }

}
