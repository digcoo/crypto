package com.binance.client.dto;

import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.market.LongCandlestickMA;
import com.binance.client.model.market.MACross;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LongMACrossCalculate {

    private static final String link_pref = "https://www.binance.com/zh-CN/futures/";

    LongCandlestickMA currentCandlestickMA;
    List<LongCandlestickDTO> periodCandlestickDTOs;

    List<MACross> allCrosses = new ArrayList<>();

    public LongMACrossCalculate(LongCandlestickMA candlestickMA, List<LongCandlestickDTO> candlestickDTOS) {
        this.currentCandlestickMA = candlestickMA;
        this.periodCandlestickDTOs = candlestickDTOS;
        calAllCrosses();
    }

    private void calAllCrosses() {
        try {
            for (LongCandlestickDTO candlestickDTO: periodCandlestickDTOs) {

                PeriodTypeEnum periodType = candlestickDTO.getPeriodTypeEnum();
                List<LongCandlestickMA> candlestickMAS = candlestickDTO.getCandlestickMAS();

                for (int i = 1; i < candlestickMAS.size() - 1; i++) {
                    LongCandlestickMA trade0 = candlestickMAS.get(i);
                    LongCandlestickMA tradeBefore0 = candlestickMAS.get(i-1);
                    LongCandlestickMA tradeAfter0 = candlestickMAS.get(i+1);

                    trade0.setLastCandlestickMA(tradeBefore0);
                    trade0.setAfterCandlestickMA(tradeAfter0);

                    List<MACross> maCrosses = trade0.getMACrosses();
                    if (maCrosses != null && maCrosses.size() > 0) {
                        this.allCrosses.addAll(maCrosses);
                    }
                }
            }
        }catch (Exception ex) {
            log.error("[calAllCrosses] exception..., symbol = {}", currentCandlestickMA.getSymbol(), ex);
        }
    }

    public boolean ifCrossMACross(List trades) {
        if ("WAVESUSDT".equals(currentCandlestickMA.getSymbol())) {
            log.debug("fdsafdsa");
        }
        for (Object obj: trades) {
            LongCandlestickMA trade = (LongCandlestickMA)obj;

            if (trade.getShiTiRate().compareTo(BigDecimal.ZERO) < 0) {
                continue;
            }

            for (MACross maCross: allCrosses) {
                if (maCross.isContain(trade)) {
                    try {
                        log.warn("CrossMACross多命中: crossDay=[{},{}], MACross=[{}, {}:{}, {}, {}], {}"
                                , trade.getSymbol(), trade.getOpenTimeStr().substring(0, 13)
                                , maCross.getPeriodType(), maCross.getOpenTimeStr().substring(0, 13), maCross.getMa1().getMaType(), maCross.getMa2().getMaType(), maCross.getCrossPointY()
                                , link_pref + trade.getSymbol());
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }

                if (maCross.isAfter(trade)) {
                    continue;
                }

                if (trade.getOpen().compareTo(maCross.getCrossPointY()) < 0
                    && trade.getClose().compareTo(maCross.getCrossPointY()) > 0) {
                    try {
                        log.warn("CrossMACross多命中: crossDay=[{},{}], MACross=[{}, {}:{}, {}, {}], {}"
                                , trade.getSymbol(), trade.getOpenTimeStr().substring(0, 13)
                                , maCross.getPeriodType(), maCross.getOpenTimeStr().substring(0, 13), maCross.getMa1().getMaType(), maCross.getMa2().getMaType(), maCross.getCrossPointY()
                                , link_pref + trade.getSymbol());
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
