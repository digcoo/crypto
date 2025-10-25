package com.digcoo.fitech.quant.observer.base;

import com.digcoo.fitech.common.model.Trade;

public interface TradeObserver {
    void onTradeUpdate(String symbol, Trade trade);
}
