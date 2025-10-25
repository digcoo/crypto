package com.digcoo.fitech.stock.ws;

import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.util.http.SinaHttpClient;
import com.digcoo.fitech.stock.ws.base.StockWebsocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class SinaWebsocketClientImpl extends StockWebsocketClient {

    SinaHttpClient sinaHttpClient;

    public SinaWebsocketClientImpl(SinaHttpClient sinaHttpClient) {
        super(3);
        this.sinaHttpClient = sinaHttpClient;
    }

    @Override
    public List<Candlestick> refreshCandlestick(List<String> symbols) {
        try {

            return sinaHttpClient.requestRealtimeCandles(symbols);

        }catch (Exception ex) {
            log.error("error sina requestRealtimeData", ex);
        }

        return null;
    }

}
