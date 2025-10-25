package com.digcoo.fitech.stock;

import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.util.http.SinaHttpClient;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;

public class SinaHttpClientTest {



    @Test
    public void requestPageTest() {
        SinaHttpClient sinaHttpClient = new SinaHttpClient(HttpClient.newHttpClient());
        List<Candlestick> candlesticks = sinaHttpClient.requestAllSymbols("sh_a");
    }

}
