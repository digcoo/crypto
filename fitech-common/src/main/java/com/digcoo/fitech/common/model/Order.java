package com.digcoo.fitech.common.model;


import com.digcoo.fitech.common.enums.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

@Data
@Builder
public class Order {
    private String orderId;
    private String symbol;
    private OrderSide orderSide;
    private PositionSide positionSide;
    private OrderType orderType;
    private BigDecimal qty;
    private BigDecimal price;
    private BigDecimal executedPrice;
    private OrderState status;
    private boolean isOpen; //是否开仓
    private BigDecimal tpPrice; //止盈价格
    private BigDecimal slPrice; //止损价格

    public LinkedHashMap<String, Object> toMap() {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("side", orderSide);
        parameters.put("type", orderType);
        parameters.put("positionSide", positionSide);
        parameters.put("timeInForce", TimeInForce.GTC);
        parameters.put("quantity", qty);
        parameters.put("price", price);
        return parameters;
    }
}
