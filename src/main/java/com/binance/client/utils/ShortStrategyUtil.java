package com.binance.client.utils;

import com.binance.client.dto.ShortCandlestickDTO;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.LongCandlestickMA;
import com.binance.client.model.market.ShortCandlestickMA;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
public final class ShortStrategyUtil {
    public static final String LINK_PREF = "https://www.binance.com/zh-CN/futures/";

    public static ShortCandlestickDTO buildShortCandlestickDTO(List<Candlestick> candlesticks, PeriodTypeEnum periodTypeEnum) {
        if (candlesticks == null || candlesticks.size() == 0) {
            return null;
        }
        List<ShortCandlestickMA> candlestickMAs = new ArrayList<>();

        //计算MA
        for (int i = 5; i <= candlesticks.size(); i++) {
            try {
                ShortCandlestickMA candlestickMA = ShortCandlestickMA.copyFrom(candlesticks.get(i - 1));
                candlestickMA.setPeriodType(periodTypeEnum);
                //计算MA5
                if (i >= 5) {
                    Double ma5 = candlesticks.subList(i - 5 < 0 ? 0 : i - 5, i).stream().mapToDouble(x -> x.getClose().doubleValue()).average().orElse(0.0);
                    candlestickMA.setMa5(new BigDecimal(ma5).setScale(candlestickMA.getPrecision() + 5, RoundingMode.HALF_UP));
                }
                if (i >= 10) {
                    Double ma10 = candlesticks.subList(i - 10 < 0 ? 0 : i - 10, i).stream().mapToDouble(x -> x.getClose().doubleValue()).average().orElse(0.0);
                    candlestickMA.setMa10(new BigDecimal(ma10).setScale(candlestickMA.getPrecision() + 5, RoundingMode.HALF_UP));
                }
                if (i >= 20) {
                    Double ma20 = candlesticks.subList(i - 20 < 0 ? 0 : i - 20, i).stream().mapToDouble(x -> x.getClose().doubleValue()).average().orElse(0.0);
                    candlestickMA.setMa20(new BigDecimal(ma20).setScale(candlestickMA.getPrecision() + 5, RoundingMode.HALF_UP));
                }
                if (i >= 30) {
                    Double ma30 = candlesticks.subList(i - 30 < 0 ? 0 : i - 30, i).stream().mapToDouble(x -> x.getClose().doubleValue()).average().orElse(0.0);
                    candlestickMA.setMa30(new BigDecimal(ma30).setScale(candlestickMA.getPrecision() + 5, RoundingMode.HALF_UP));
                }
                candlestickMAs.add(candlestickMA);
//                log.info("symbol=[{}], period=[{}:{}～{}], ma5~m30=[{}:{}:{}:{}]", candlestickMA.getSymbol(), candlestickMA.getPeriodTypeEnum(), candlestickMA.getOpenTimeStr(), candlestickMA.getCloseTimeStr(), candlestickMA.getMa5(), candlestickMA.getMa10(), candlestickMA.getMa20(), candlestickMA.getMa30());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        candlestickMAs = candlestickMAs.stream().sorted(Comparator.comparing(ShortCandlestickMA::getCloseTime)).collect(Collectors.toList());
        return ShortCandlestickDTO.builder()
                .symbol(candlesticks.get(0).getSymbol())
                .periodTypeEnum(periodTypeEnum)
                .candlestickMAS(candlestickMAs)
                .build();
    }


    public static boolean filterShortCandlestick(ShortCandlestickDTO... candlestickDTOS) {
        boolean bid = true;
        String symbol = candlestickDTOS[0].getSymbol();
        for (ShortCandlestickDTO candlestickDTO: candlestickDTOS) {

            ShortCandlestickMA candlestick0 = candlestickDTO.getLastCandlestick(0);
            ShortCandlestickMA candlestick1 = candlestickDTO.getLastCandlestick(-1);
            if (candlestick0 == null || candlestick1 == null) {
                continue;
            }

            //过滤阴线压顶
            bid = bid && !(candlestick1!= null && candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                            && candlestick0.getClose().compareTo(candlestick1.getShiTiMin()) > 0);

            //过滤阴线盖头（前一日收红，当前绿线压住前日实体）
            bid = bid && !(candlestick1!= null && candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                    && candlestick0.getClose().compareTo(candlestick1.getShiTiMax()) > 0);

            //过滤日/周/月双绿
            bid = bid && !(candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                            && (candlestick1 == null
                                || (candlestick1.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                                    && candlestick0.getClose().compareTo(candlestick1.getShiTiMax().multiply(new BigDecimal("0.97"))) > 0)));

            //过滤水下收绿
            bid = bid && !(candlestick0.getMaxMA() != null && candlestick0.getClose().compareTo(candlestick0.getMaxMA()) > 0
                            && candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) < 0);

            //过滤头顶收绿
            bid = bid && !(candlestick0.getMinMA() != null && candlestick0.getClose().compareTo(candlestick0.getMinMA()) < 0
                            && candlestick0.getShiTiRate().compareTo(BigDecimal.ZERO) > 0);

        }

        //顺方向周期涨幅:取出candlestickDTOS的periodTypeEnum为WEEK的记录，最近5个周期的涨幅 > 5%
        AtomicBoolean zhangfuRate = new AtomicBoolean(false);
        Arrays.stream(candlestickDTOS)
                .filter(candlestickDTO -> candlestickDTO.getPeriodTypeEnum() == PeriodTypeEnum.WEEK).findAny()
                .ifPresent(candlestickDTO -> {
                    zhangfuRate.set(candlestickDTO.getLastCandlesticks(5).stream()
//                            .anyMatch(x -> x.getShiTiRate().compareTo(new BigDecimal(0.04).negate()) < 0));
                            .anyMatch(x -> x.getShiTiRate().abs().compareTo(new BigDecimal(0.04)) > 0));
                });

        bid = bid && zhangfuRate.get();

//        if (bid) {
//            log.error("空命中: {}, {}", symbol, LINK_PREF+symbol);
//        }
        return bid;
    }

}
