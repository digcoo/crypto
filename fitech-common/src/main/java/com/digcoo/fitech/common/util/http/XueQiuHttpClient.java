package com.digcoo.fitech.common.util.http;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.digcoo.fitech.common.enums.CandlestickPeriod;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Component
public class XueQiuHttpClient {

    String xueqiuCookie = "cookiesu=101729952232908; smidV2=20241026221713104a2226325c50c359eb9cd0757c099200d422d3b85e955d0; s=cb1diqv40c; device_id=1087b84935b10a1740504ea35d0cd6e6; acw_tc=ac11000117475634459681955e00672a830befa56369d5037f009d1b330e2f; xq_a_token=75116a2a5439edb58d3d99533cfbc4d72e0ee819; xqat=75116a2a5439edb58d3d99533cfbc4d72e0ee819; xq_r_token=521f1781edc2a09cffdf7d59b5b3fe37c1c1f577; xq_id_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOi0xLCJpc3MiOiJ1YyIsImV4cCI6MTc0OTk0OTc1MywiY3RtIjoxNzQ3NTYzNDI5MjU5LCJjaWQiOiJkOWQwbjRBWnVwIn0.QG47CPtwKicSARROfiy_trlYpAcI0_DqK9k3a7oDXJTw8rJ6iquS_rpyq00VEVDw-p3Ix6QTxJo3LwTvonNfn6Sl-qacZYTfjc5ODFOmRdCRX5xEf2G1o7mEKnYhVlUo2qULWIZzYr1oRzCkUzGhq89YCmPXpt_Uckm-HfftWkKqcxUCEMUgJ73wZQ50eSxPsEbumly5Ykcm1jyHQhDZ97oRp4Cm2knF9RuUOHqp1xW6IRLVUoo30PjnrMC6uIzDHko1aDQL5n-KLlA9hf0LijfWSHaeslr9479RSLtXBRawehkzvqf4LFG2AAtWaHqykrxFXxA56Zqa8WcFQICJDQ; u=101729952232908; Hm_lvt_1db88642e346389874251b5a1eded6e3=1747563447; HMACCOUNT=5E156ED48B683EF5; is_overseas=0; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1747563455; .thumbcache_f24b8bbe5a5934237bbc0eda20c1b6e7=rsKBsLYXDuUlHkEoonjnRU/GDKCy53qgzyREk33nryVWNdl9Ed44ulS3lkPsmJW9SwEFbb53RZn54zH8vMLDSw%3D%3D; ssxmod_itna=QqjxnDyDBDRD2AD0DfxeIqmqxWKkT+CQDYq0dGM9Deq7UrGcD8hx0px8KT5mmo+he7B=Em+btD0vq+kDBFQDax7fb2QYgYxeWCmroqe7CCnGhYY2egjYu3vCI2emLdst1mbK3xAE3DKqGm84DS9DD93feD44DvDBYD74G+DDeDirrDGdTqyDDFAj2YLCX=Kj4qEeDEC8m=Dit7xiviyjT3ywxihfoFRCqVDY=DQdB=KhbDj4FF4KGy84GWiRrv1KGuUaiVAgb=gCwAdOGuYXel70YgC7DY00j4ndt57BZsF3XYnS5Kn4wwt5D2qDGbGxnDqGxpA87Tk=hko3KogQWCtp74tY4krwOeGMA5VB3rAQeO47iqoYt3e17iDD; ssxmod_itna2=QqjxnDyDBDRD2AD0DfxeIqmqxWKkT+CQDYq0dGM9Deq7UrGcD8hx0px8KT5mmo+he7B=Em+bDGfnDQpRQx03t+GBgRbr3pPM9I=gh05ioKQimMOxD";
    //    String baseUrl = "https://stock.xueqiu.com/v5/stock/chart/kline.json?symbol=SZ000001&begin=1745764450122&period=day&type=before&count=-284&indicator=kline,pe,pb,ps,pcf,market_capital,agt,ggt,balance";
    String xueqiuBaseUrl = "https://stock.xueqiu.com/v5/stock/chart/kline.json?indicator=kline,pe,pb,ps,pcf,market_capital,agt,ggt,balance&type=before&symbol=";

    String xueqiuCookieUrl = "https://xueqiu.com/";

    private  final HttpClient httpClient;
    public XueQiuHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<Candlestick> getHistoryCandles(String symbol, CandlestickPeriod period, int limit) {
        try {

            String apiUrl = getApiUrl(symbol, period, limit);

            String jsonResponse = retryGetByUrl(apiUrl);

            return parseCandlestick(symbol, period, jsonResponse);

        } catch (Exception ex) {
            log.error("error XueQiu requestData...symbol: {}, period: {}", symbol, period, ex);
            System.exit(-1);
        }
        return Collections.emptyList();
    }

    private List<Candlestick> parseCandlestick(String symbol, CandlestickPeriod period, String jsonResponse) {
        if (StringUtils.isNotBlank(jsonResponse) && !jsonResponse.equals("null")) {
            JSONArray jsonArray = JSON.parseObject(jsonResponse).getJSONObject("data").getJSONArray("item");
            if (!jsonArray.isEmpty()) {
                List<Candlestick> candlesticks = new ArrayList<>(jsonArray.size());
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONArray itemJsonArray = jsonArray.getJSONArray(i);

                    long timestamp = itemJsonArray.getLong(0);
                    Pair<Long, Long> firstAndLastDayOfPeriod = DateUtil.resetToFirstAndLastDayOfPeriod(timestamp, period);
                    long openTime = DateUtil.resetTo0930(firstAndLastDayOfPeriod.getLeft());
                    long closeTime = DateUtil.resetTo1500(firstAndLastDayOfPeriod.getRight());

                    candlesticks.add(Candlestick.builder()
                                    .symbol(symbol)
                                    .period(period)
                                    .openTime(openTime)
                                    .closeTime(closeTime)
                                    .timestamp(timestamp)
                                    .volume(itemJsonArray.getBigDecimal(1))
                                    .open(itemJsonArray.getBigDecimal(2))
                                    .high(itemJsonArray.getBigDecimal(3))
                                    .low(itemJsonArray.getBigDecimal(4))
                                    .close(itemJsonArray.getBigDecimal(5))
                                    .lastClose(itemJsonArray.getBigDecimal(5).subtract(itemJsonArray.getBigDecimal(6)))
                                    .changeRate(itemJsonArray.getBigDecimal(7))
                                    .turnoverRate(itemJsonArray.getBigDecimal(8))
                                    .amount(itemJsonArray.getBigDecimal(9))
                            .build());
                }

                return candlesticks;
            }
        }

        return Collections.emptyList();
    }


    private String retryGetByUrl(String apiUrl) throws Exception {
        int retryCount = 3;
        for (int i = 0; i < retryCount; i++) {
            try {
                return getByUrl(apiUrl);
            } catch (Exception ex) {
                log.error("error XueQiu requestByUrl...apiUrl: {}", apiUrl, ex);
                TimeUnit.SECONDS.sleep(20);
            }
        }
        throw new RuntimeException("error XueQiu requestByUrl...apiUrl: " + apiUrl);
    }

    private String getByUrl(String apiUrl) throws InterruptedException, IOException {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Cookie", xueqiuCookie)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("error XueQiu requestByUrl, status code:{}, errMsg:\n{}", response.statusCode(), response.body());
                return null;
            }

            return response.body();

    }

    public String getCookie() throws InterruptedException, IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(xueqiuCookieUrl))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("error XueQiu requestByUrl, status code:{}, errMsg:\n{}", response.statusCode(), response.body());
            return "";
        }else {
            String cookie = "";
            Map<String, List<String>> headerMap = response.headers().map();
            for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
                log.info("{} : {}", entry.getKey(), entry.getValue());

                if (entry.getKey().equalsIgnoreCase("Set-Cookie")) {
                    cookie = entry.getValue().stream().collect(Collectors.joining("; "));
                }
            }
            return cookie;
        }

    }

    private String getApiUrl(String symbol, CandlestickPeriod period, int limit) {
        return xueqiuBaseUrl + symbol.toUpperCase() + "&period=" + period.getStockPeriod() + "&count=" + (-limit) + "&begin=" + System.currentTimeMillis();
    }

    public void main(String[] args) {

    }

}
