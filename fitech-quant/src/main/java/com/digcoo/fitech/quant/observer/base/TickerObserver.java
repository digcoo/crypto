package com.digcoo.fitech.quant.observer.base;

import com.digcoo.fitech.common.model.ticker.Ticker24H;

public interface TickerObserver {
    void onTickerUpdate(String symbol, Ticker24H ticker);

}
