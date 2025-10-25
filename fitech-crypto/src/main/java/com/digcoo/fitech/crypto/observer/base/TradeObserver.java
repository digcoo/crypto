package com.digcoo.fitech.crypto.observer.base;

import com.digcoo.fitech.common.model.Trade;

public interface TradeObserver {
    void onTradeUpdate(String symbol, Trade trade);
}
