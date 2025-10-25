package com.digcoo.fitech.stock.observer.base;

import com.digcoo.fitech.common.model.Trade;

public interface TradeObserver {
    void onTradeUpdate(String symbol, Trade trade);
}
