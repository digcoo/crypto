package com.digcoo.fitech.common.param;

import com.digcoo.fitech.common.enums.OrderSide;
import com.digcoo.fitech.common.enums.OrderType;
import com.digcoo.fitech.common.enums.PositionSide;
import com.digcoo.fitech.common.enums.TimeInForce;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;


@Data
@Builder
public class DelegateOrderParam {
    String symbol;
    OrderSide orderSide;
    PositionSide positionSide;
    OrderType orderType;
    BigDecimal delegatePrice;
    BigDecimal qty;
    BigDecimal positionValue;

    private BigDecimal currentPrice;
    private BigDecimal tpPrice;
    private BigDecimal slPrice;

//    /**
//     * 预设止盈触发价格
//     */
//    private BigDecimal presetTakeProfitPrice;
//
//    /**
//     * 预设止盈执行价格
//     */
//    private BigDecimal exeTakeProfitPrice;
//
//    /**
//     * 预设止损触发价格
//     */
//    private BigDecimal presetStopLossPrice;
//
//    /**
//     * 预设止损执行价格
//     */
//    private BigDecimal exeStopLossPrice;
//
//    /**
//     * 预设止盈价格触发类型（合约使用）
//     */
//    private Integer presetTppTriType;
//
//    /**
//     * 预设止损价格触发类型（合约使用）
//     */
//    private Integer presetSlpTriType;

    public LinkedHashMap<String, Object> toMap() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("side", orderSide);
        parameters.put("type", orderType);
        parameters.put("positionSide", positionSide);
        parameters.put("timeInForce", TimeInForce.GTC);
        parameters.put("quantity", qty);
        parameters.put("price", delegatePrice);
        return parameters;
    }

}
