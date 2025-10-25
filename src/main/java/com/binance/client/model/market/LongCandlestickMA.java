package com.binance.client.model.market;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.binance.client.enums.PeriodTypeEnum;
import com.binance.client.model.enums.MAType;
import com.binance.client.utils.MathUtil;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Data
@ToString
@SuperBuilder
@Slf4j
@JSONType(ignores = {"lastCandlestickMA", "afterCandlestickMA"})
public class LongCandlestickMA extends Candlestick {

    private BigDecimal ma5;

    private BigDecimal ma10;

    private BigDecimal ma20;

    private BigDecimal ma30;

    private LongCandlestickMA lastCandlestickMA;

    private LongCandlestickMA afterCandlestickMA;

    private PeriodTypeEnum periodType;


    @JSONField(deserialize=false, serialize = false)
    public List<BigDecimal> getAllMAs() {
        return Arrays.asList(this.ma5, this.ma10, this.ma20, this.ma30).stream().filter(x -> Objects.nonNull(x)).collect(Collectors.toList());
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getShiTiRate(){
        return getClose().subtract(getOpen()).divide(getOpen(), getPrecision() + 1, RoundingMode.HALF_UP);
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getShiTiMax(){
        return MathUtil.max(this.getOpen(), this.getClose());
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getShiTiMin(){
        return MathUtil.min(this.getOpen(), this.getClose());
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getMinMA() {
        return MathUtil.min(this.ma5, this.ma10, this.ma20, this.ma30);
    }

    @JSONField(deserialize=false, serialize = false)
    public BigDecimal getMaxMA() {
        return MathUtil.max(this.ma5, this.ma10, this.ma20, this.ma30);
    }

    @JSONField(deserialize=false, serialize = false)
    private List<MACross.MALine> getMALines() {
        List<MACross.MALine> maLines = new ArrayList<>();
        if (this.lastCandlestickMA == null) {
            return maLines;
        }
        if (this.ma5 != null && this.lastCandlestickMA.getMa5() != null) {
            maLines.add(new MACross.MALine(this.periodType, MAType.MA5, Pair.of(lastCandlestickMA.getMa5(), this.ma5), this.getOpenTime()));
        }
        if (this.ma10 != null && this.lastCandlestickMA.getMa10() != null) {
            maLines.add(new MACross.MALine(this.periodType, MAType.MA10, Pair.of(lastCandlestickMA.getMa10(), this.ma10), this.getOpenTime()));
        }
        if (this.ma20 != null && this.lastCandlestickMA.getMa20() != null) {
            maLines.add(new MACross.MALine(this.periodType, MAType.MA20, Pair.of(lastCandlestickMA.getMa20(), this.ma20), this.getOpenTime()));
        }
        if (this.ma30 != null && this.lastCandlestickMA.getMa30() != null) {
            maLines.add(new MACross.MALine(this.periodType, MAType.MA30, Pair.of(lastCandlestickMA.getMa30(), this.ma30), this.getOpenTime()));
        }
        return maLines;
    }

    @JSONField(deserialize=false, serialize = false)
    public List<MACross> getMACrosses() {
        List<MACross> maCrossList = new ArrayList<>();
        List<MACross.MALine> maLines = getMALines();
        for (int i = 0; i < maLines.size(); i++) {
            for (int j = i+1; j < maLines.size(); j++) {
                MACross.MALine maLine1 = maLines.get(i);
                MACross.MALine maLine2 = maLines.get(j);
                BigDecimal crossY = MathUtil.calCrossPointY(maLine1, maLine2);
                if (null != crossY) {
                    maCrossList.add(new MACross(this.periodType, maLine1, maLine2, crossY, this.getOpenTime()));
                }
            }
        }
        return maCrossList;
    }

    /**
     * 上MA线的上下边界值
     * @param topN
     * @Param ifInvert  是否翻转
     * @return
     */
    public Pair<BigDecimal, BigDecimal> getUpMA(Integer topN) {
        List<BigDecimal> allMAs = getAllMAs();
        if (allMAs == null) {
            return null;
        }
        List<BigDecimal> mas = allMAs.stream()
                .sorted(Comparator.comparing(BigDecimal::doubleValue).reversed())   //降序
                .collect(Collectors.toList());

        if (topN == null) {
            return Pair.of(MathUtil.min(mas), MathUtil.max(mas));
        }

        if (mas.size() > topN) {
            mas = mas.subList(0, topN);
        }
        return Pair.of(MathUtil.min(mas), MathUtil.max(mas));
    }


    /**
     * 上MA线的上下边界值
     * @param topN
     * @return
     */
    public Pair<BigDecimal, BigDecimal> getDownMA(Integer topN) {
        List<BigDecimal> allMAs = getAllMAs();
        if (allMAs == null) {
            return null;
        }
        List<BigDecimal> mas = allMAs.stream()
                .sorted(Comparator.comparing(BigDecimal::doubleValue))   //升序
                .collect(Collectors.toList());

        if (topN == null) {
            return Pair.of(MathUtil.min(mas), MathUtil.max(mas));
        }

        if (mas.size() > topN) {
            mas = mas.subList(0, topN);
        }
        return Pair.of(MathUtil.min(mas), MathUtil.max(mas));
    }

    public Boolean ifCrossUpMA(int topN) {
        Pair<BigDecimal, BigDecimal> maPair = getUpMA(topN);
        if (maPair == null){
            return false;
        }
        return (this.getLow().compareTo(maPair.getLeft()) <= 0
                    || (this.lastCandlestickMA != null && this.lastCandlestickMA.getLow().compareTo(this.lastCandlestickMA.getUpMA(topN).getLeft()) <= 0))
                && this.getClose().compareTo(maPair.getRight()) > 0
                && this.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                ;
    }

    public Boolean ifCrossDownMA(int topN) {
        Pair<BigDecimal, BigDecimal> maPair = getDownMA(topN);
        if (maPair == null){
            return false;
        }
        return (this.getLow().compareTo(maPair.getLeft()) <= 0
                    || (this.lastCandlestickMA != null && this.lastCandlestickMA.getLow().compareTo(this.lastCandlestickMA.getDownMA(topN).getLeft()) <= 0))
                && this.getClose().compareTo(maPair.getRight()) > 0
                && this.getShiTiRate().compareTo(BigDecimal.ZERO) > 0
                ;
    }

    public Boolean ifCrossAnyMA(int num) {
        if (this.getShiTiRate().compareTo(this.periodType.getTiziHeight()) < 0) {
            return false;
        }
        return calCrossMACount(getAllMAs(), this) >= num;
    }

    @JSONField(deserialize=false, serialize = false)
    public Boolean ifSingleRed() {
        return this.lastCandlestickMA != null && this.lastCandlestickMA.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                && this.afterCandlestickMA != null && this.afterCandlestickMA.getShiTiRate().compareTo(BigDecimal.ZERO) < 0
                && this.getShiTiRate().compareTo(this.periodType.getTiziHeight()) > 0
                && this.getZhenFuRate().compareTo(this.periodType.getTiziZhenFuRate()) > 0;
    }

    public Boolean ifFanbao(boolean ifXiayi) {
        return this.getClose().compareTo(lastCandlestickMA.getShiTiMax()) > 0
                && (this.getShiTiRate().compareTo(this.periodType.getTiziHeight()) > 0
                    //两日涨幅
                    || this.getClose().subtract(this.lastCandlestickMA.getShiTiMin()).divide(this.lastCandlestickMA.getShiTiMin(), 5, RoundingMode.DOWN).compareTo(this.periodType.getTiziHeight()) > 0)
                && (!ifXiayi || lastCandlestickMA.getShiTiRate().compareTo(BigDecimal.ZERO) < 0);
    }

    public Integer calCrossMACount(List<BigDecimal> mas, LongCandlestickMA candlestickMA) {
        if (mas == null || mas.size() == 0) {
            return 4;
        }

        if (candlestickMA.getShiTiRate().compareTo(this.periodType.getTiziHeight()) < 0) {
            return 0;
        }

        mas = mas.stream().sorted(Comparator.comparingDouble(BigDecimal::doubleValue)).collect(Collectors.toList());

        Map<Pair<BigDecimal, BigDecimal>, Integer> map = new HashMap<>();
        for (int i = 0; i <= mas.size(); i++) {
            if (i == 0){
                map.put(Pair.of(new BigDecimal(Double.MIN_VALUE), mas.get(i)), i);
            }else if(i == mas.size()) {
                map.put(Pair.of(mas.get(i-1), new BigDecimal(Double.MIN_VALUE)), i);
            }else {
                map.put(Pair.of(mas.get(i-1), mas.get(i)), i);
            }
        }

        Pair<BigDecimal, BigDecimal> pricePair = Pair.of(MathUtil.min(candlestickMA.getLastCandlestickMA().getLow(), candlestickMA.getLow()), candlestickMA.getClose());

        int stepLowIndex = 0;
        int stepHighIndex = 0;
        for (Map.Entry<Pair<BigDecimal, BigDecimal>, Integer> entry : map.entrySet()) {
            if (pricePair.getLeft().compareTo(entry.getKey().getLeft()) > 0
                    && pricePair.getLeft().compareTo(entry.getKey().getRight()) <= 0) {
                stepLowIndex = entry.getValue();
            }

            if (pricePair.getRight().compareTo(entry.getKey().getLeft()) > 0
                    && pricePair.getRight().compareTo(entry.getKey().getRight()) <= 0) {
                stepHighIndex = entry.getValue();
            }
        }

        return (4 - mas.size()) + (stepHighIndex - stepLowIndex);

    }

    public static LongCandlestickMA copyFrom(Candlestick candlestick) {
        return LongCandlestickMA.builder()
                .symbol(candlestick.getSymbol())
                .open(candlestick.getOpen())
                .high(candlestick.getHigh())
                .low(candlestick.getLow())
                .close(candlestick.getClose())
                .openTime(candlestick.getOpenTime())
                .closeTime(candlestick.getCloseTime())
                .quoteAssetVolume(candlestick.getQuoteAssetVolume())
                .takerBuyBaseAssetVolume(candlestick.getTakerBuyBaseAssetVolume())
                .takerBuyQuoteAssetVolume(candlestick.getTakerBuyQuoteAssetVolume())
                .volume(candlestick.getVolume())
                .build();
    }


}
