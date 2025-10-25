package com.digcoo.fitech.common.ws;

import com.digcoo.fitech.common.model.Candlestick;

public interface PriceUpdateCallback {
    void onReceive(Candlestick candlestick);

}
