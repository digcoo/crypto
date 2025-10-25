package com.digcoo.fitech.quant.observer.base;

import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;

import java.util.List;

public interface CandlestickObserver {
    void onCandleUpdate(String symbol, CandlestickPeriod period, List<Candlestick> candlesticks);
}
