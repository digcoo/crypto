package com.digcoo.fitech.common.model;


import com.digcoo.fitech.common.enums.OrderSide;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.math.BigDecimal;


/**
 * 交易明细
 */
@Data
@Builder
public class Trade {
    private String tradeId;
    private String orderId;
    private String symbol;
    private OrderSide side;
    private BigDecimal price;
    private BigDecimal qty;
    private BigDecimal fee;
    private String feeAsset;
    private long tradeTime;
    private BigDecimal profit;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private OrderSide orderSide;

    //开仓成交
    private boolean openDeal;
    private Order order;

    // 计算交易金额
    public BigDecimal getTradeAmount() {
        return price.multiply(qty);
    }

    public String getTradeTimeStr() {
        return DateFormatUtils.format(this.tradeTime, "yyyy-MM-dd HH:mm:ss");
    }

    public BigDecimal getBalanceAfter(BigDecimal balanceBefore) {
        if (this.side == OrderSide.BUY) {
            // 买入交易：余额减少 (价格*数量 + 手续费)
            return balanceBefore.subtract(
                    this.price.multiply(this.qty).add(this.fee)
            );
        } else {
            // 卖出交易：余额增加 (价格*数量 - 手续费)
            return balanceBefore.add(
                    this.price.multiply(this.qty).subtract(this.fee)
            );
        }
    }

    // 计算净盈亏(对于平仓交易)
    public BigDecimal getNetPnl(BigDecimal entryPrice) {
        if (side == OrderSide.BUY) {
            return BigDecimal.ZERO;
        }
        return price.subtract(entryPrice).multiply(qty).subtract(fee);
    }
}
