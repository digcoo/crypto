package com.binance.client.dto;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.market.LongCandlestickMA;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class LongTiziCalculate {

    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";

    LongCandlestickMA currentCandlestickMA;
    List<LongCandlestickDTO> periodCandlestickDTOs;
    List<LongTizi> allTizis;

    public LongTiziCalculate(LongCandlestickMA candlestickMA, List<LongCandlestickDTO> candlestickDTOS) {
        this.currentCandlestickMA = candlestickMA;
        this.periodCandlestickDTOs = candlestickDTOS;
        calAllTizis();
    }

    private void calAllTizis() {
        this.allTizis = new ArrayList<>();
        try {
            for (LongCandlestickDTO candlestickDTO: periodCandlestickDTOs) {
                PeriodTypeEnum periodTypeEnum = candlestickDTO.getPeriodTypeEnum();
                List<LongCandlestickMA> candlestickMAS = candlestickDTO.getCandlestickMAS();

                for (int i = 1; i < candlestickMAS.size() - 1; i++) {
                    LongCandlestickMA trade0 = candlestickMAS.get(i);
                    LongCandlestickMA tradeBefore0 = candlestickMAS.get(i-1);
                    LongCandlestickMA tradeAfter0 = candlestickMAS.get(i+1);
                    trade0.setLastCandlestickMA(tradeBefore0);
                    trade0.setAfterCandlestickMA(tradeAfter0);

                    if (trade0.ifCrossAnyMA(1)) {
                        if (tradeBefore0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0) {
                            this.allTizis.add(new LongTizi(currentCandlestickMA.getSymbol(), Arrays.asList(tradeBefore0, trade0), periodTypeEnum, null));
                        }else {
                            this.allTizis.add(new LongTizi(currentCandlestickMA.getSymbol(), Arrays.asList(tradeBefore0, trade0), periodTypeEnum, null));
                        }
                    } else if (trade0.ifFanbao(false)) {
                        if (tradeBefore0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0) {
                            this.allTizis.add(new LongTizi(currentCandlestickMA.getSymbol(), Arrays.asList(tradeBefore0, trade0), periodTypeEnum, null));
                        }else {
                            this.allTizis.add(new LongTizi(currentCandlestickMA.getSymbol(), Arrays.asList(tradeBefore0, trade0), periodTypeEnum, null));
                        }
                    } else if (trade0.ifSingleRed()) {
                        this.allTizis.add(new LongTizi(currentCandlestickMA.getSymbol(), Arrays.asList(trade0, trade0), periodTypeEnum, null));
                    }
                }
            }
        }catch (Exception ex) {
            log.error("[calAllTizis] exception..., symbol = {}", currentCandlestickMA.getSymbol(), ex);
        }
    }

    public boolean ifCrossTizi(List trades) {
        for (Object obj: trades) {
            LongCandlestickMA trade = (LongCandlestickMA)obj;

            if (trade.getShiTiRate().compareTo(BigDecimal.ZERO) < 0) {
                continue;
            }

            for (LongTizi tizi: allTizis) {
                if (tizi.contain(trade)) {
                    continue;
                }

                if (trade.getLow().compareTo(tizi.getOpen()) < 0
                        && trade.getClose().compareTo(tizi.getLow()) > 0) {
                    log.warn("多命中: crossDay=[{}, {}, {}], tiziPeriod=[{}, {} : {}, {} : {}], {}", trade.getSymbol(), trade.getOpenTimeStr().substring(0, 10), tizi.getClose()
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
