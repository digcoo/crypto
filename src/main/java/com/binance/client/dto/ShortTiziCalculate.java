package com.binance.client.dto;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.market.ShortCandlestickMA;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ShortTiziCalculate {

    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";

    ShortCandlestickMA currentCandlestickMA;
    List<ShortCandlestickDTO> periodCandlestickDTOs;
    List<ShortTizi> allTizis;

    public ShortTiziCalculate(ShortCandlestickMA candlestickMA, List<ShortCandlestickDTO> candlestickDTOS) {
        this.currentCandlestickMA = candlestickMA;
        this.periodCandlestickDTOs = candlestickDTOS;
        calAllTizis();
    }

    private void calAllTizis() {
        this.allTizis = new ArrayList<>();
        try {
            for (ShortCandlestickDTO candlestickDTO: periodCandlestickDTOs) {
                PeriodTypeEnum periodTypeEnum = candlestickDTO.getPeriodTypeEnum();
                List<ShortCandlestickMA> candlestickMAS = candlestickDTO.getCandlestickMAS();

                for (int i = 1; i < candlestickMAS.size() - 1; i++) {
                    ShortCandlestickMA trade0 = candlestickMAS.get(i);
                    ShortCandlestickMA tradeBefore0 = candlestickMAS.get(i-1);
                    ShortCandlestickMA tradeAfter0 = candlestickMAS.get(i+1);

                    trade0.setLastCandlestickMA(tradeBefore0);
                    trade0.setAfterCandlestickMA(tradeAfter0);
//                    if (currentCandlestickMA.getSymbol().equals("BAKEUSDT") && trade0.getOpenTimeStr().startsWith("2023-09-03")){
//                        log.info("======");
//                    }

                    if (trade0.ifCrossAnyMA(1)) {
                        if (tradeBefore0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0) {
                            this.allTizis.add(new ShortTizi(currentCandlestickMA.getSymbol(), Arrays.asList(tradeBefore0, trade0), periodTypeEnum, null));
                        }else {
                            this.allTizis.add(new ShortTizi(currentCandlestickMA.getSymbol(), Arrays.asList(tradeBefore0, trade0), periodTypeEnum, null));
                        }
                    } else if (trade0.ifFanbao(false)) {
                        if (tradeBefore0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0) {
                            this.allTizis.add(new ShortTizi(currentCandlestickMA.getSymbol(), Arrays.asList(tradeBefore0, trade0), periodTypeEnum, null));
                        }else {
                            this.allTizis.add(new ShortTizi(currentCandlestickMA.getSymbol(), Arrays.asList(tradeBefore0, trade0), periodTypeEnum, null));
                        }
                    } else if (trade0.ifSingleRed()) {
                        this.allTizis.add(new ShortTizi(currentCandlestickMA.getSymbol(), Arrays.asList(trade0, trade0), periodTypeEnum, null));
                    }
                }
            }
        }catch (Exception ex) {
            log.error("[calAllTizis] exception..., symbol = {}", currentCandlestickMA.getSymbol(), ex);
        }
    }

    public boolean ifCrossTizi(List trades) {

//        if ("C98USDT".equals(currentCandlestickMA.getSymbol())) {
//            log.info("test");
//        }

        for (Object obj: trades) {
            ShortCandlestickMA trade = (ShortCandlestickMA)obj;

            if (trade.getShiTiRate().compareTo(BigDecimal.ZERO) > 0) {
                continue;
            }

            for (ShortTizi tizi: allTizis) {

//                log.info("tizi : {},  [{}:{}:{}], [{}:{}]", currentCandlestickMA.getSymbol(), tizi.getPeriodTypeEnum(), tizi.getStartTrade().getOpenTimeStr(), tizi.getEndTrade().getCloseTimeStr(),
//                        tizi.getOpen(), tizi.getClose()
//                );

                if (tizi.contain(trade)) {
                    continue;
                }

                if (trade.getHigh().compareTo(tizi.getOpen()) > 0
                        && trade.getClose().compareTo(tizi.getHigh()) < 0) {
                    log.error("空命中: crossDay=[{}, {}, {}], tiziPeriod=[{}, {} : {}, {} : {}], {}", trade.getSymbol(), trade.getOpenTimeStr().substring(0, 10), tizi.getClose()
                            , tizi.getPeriodTypeEnum(), tizi.getStartTrade().getOpenTimeStr().substring(0, 13), tizi.getEndTrade().getOpenTimeStr().substring(0, 13)
                            , tizi.getStartTrade().getOpen(), tizi.getEndTrade().getClose()
                            , link_pref + trade.getSymbol());
                    return true;
                }
            }
        }
        return false;
    }

}
