package com.digcoo.fitech.common.util.http;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.digcoo.fitech.common.config.Config;
import com.digcoo.fitech.common.constants.GlobalConstants;
import com.digcoo.fitech.common.model.Candlestick;
import com.digcoo.fitech.common.model.OrderBook;
import com.digcoo.fitech.common.util.DateUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class SinaHttpClient {


    //https://vip.stock.finance.sina.com.cn/mkt/#stock_sh_up
    String sinaPageUrl = "https://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?num=80&sort=changepercent&asc=0&symbol=&_s_r_a=page&node=";

    //
    String sinaRealtimeUrl = "http://hq.sinajs.cn/list=";

    private final HttpClient httpClient;


    @Resource
    private Config config;
    public SinaHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }


    /**
     * 获取上交所的数据
     * @param exchange
     * @return
     */
    public List<Candlestick> requestAllSymbols(String exchange) {
        try {

            List<Candlestick> allSymbols = new ArrayList<>();
            int page = 1;
            while(page <= 1000) {

                String apiUrl = getApiUrl(exchange, page);

                String jsonResponse = retryRequestByUrl(apiUrl);

                List<Candlestick> candlesticks = parseSymbols(jsonResponse);

                if (CollectionUtils.isEmpty(candlesticks)) {
                    break;
                }

                allSymbols.addAll(candlesticks);

                page++;

                TimeUnit.MILLISECONDS.sleep(config.getSpiderIntervalMilSeconds());

//                break;

            }

            return allSymbols;

        }catch (Exception ex) {
            log.error("error Sina requestAllSymbols, exchange: {}", exchange, ex);
        }
        return Collections.emptyList();
    }

    /**
     * 获取实时数据(多symbol)
     * @param symbols
     * @return
     */
    public List<Candlestick> requestRealtimeCandles(List<String> symbols) {
        try {

            String apiUrl = sinaRealtimeUrl + String.join(",", symbols);
            String jsonResponse = retryRequestByUrl(apiUrl);

            return parseRealtimeCandlesticks(jsonResponse);

        }catch (Exception ex) {
            log.error("error Sina requestRealtimeData, symbols: {}", symbols, ex);
        }

        return Collections.emptyList();
    }

    private List<Candlestick> parseSymbols(String jsonResponse) {
        if (StringUtils.isNotBlank(jsonResponse) && !jsonResponse.equals("null") && !jsonResponse.equals("[]")) {
            List<Candlestick> candlesticks = new ArrayList<>(200);
            JSONArray jsonArray = JSON.parseArray(jsonResponse);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                candlesticks.add(Candlestick.builder()
                                .symbol(jsonObject.getString("symbol"))
                                .name(jsonObject.getString("name"))
                                .st(jsonObject.getString("name").toLowerCase().contains("st"))
                                .close(jsonObject.getBigDecimal("trade"))
                                .open(jsonObject.getBigDecimal("open"))
                                .high(jsonObject.getBigDecimal("high"))
                                .low(jsonObject.getBigDecimal("low"))
                                .volume(jsonObject.getBigDecimal("volume"))
                                .amount(jsonObject.getBigDecimal("amount"))
                                .lastClose(jsonObject.getBigDecimal("settlement"))
                                .changeRate(jsonObject.getBigDecimal("changepercent"))
                                .turnoverRate(jsonObject.getBigDecimal("turnoverratio"))
                                .build());
            }
            return candlesticks;
        }
        return Collections.emptyList();
    }

    private List<Candlestick> parseRealtimeCandlesticks(String lineText) throws ParseException {
        List<Candlestick> candlesticks = new ArrayList<>();
        if(StringUtils.isNotBlank(lineText) && !lineText.equals("null")) {
            String[] lines = lineText.split("\n");
            for (String line : lines) {
                Candlestick candlestick = parseLine(line);
                candlesticks.add(candlestick);
            }
        }

        return candlesticks;
    }

    private Candlestick parseLine(String lineText) throws ParseException {

        String data = lineText.substring(lineText.indexOf("=") + 2, lineText.lastIndexOf(";"));
        String[] split = data.split(",");

        String symbol = lineText.substring(lineText.indexOf("hq_str_") + "hq_str_".length(), lineText.indexOf("="));
        long timestamp = DateUtils.parseDate(split[30] + " " + split[31], "yyyy-MM-dd HH:mm:ss").getTime();

        long openTime = DateUtil.resetTo0930(timestamp);
        long closeTime = DateUtil.resetTo1500(timestamp);

        return Candlestick.builder()
                .symbol(symbol)
                .name(split[0])
                .open(new BigDecimal(split[1]))
                .lastClose(new BigDecimal(split[2]))
                .close(new BigDecimal(split[3]))
                .high(new BigDecimal(split[4]))
                .low(new BigDecimal(split[5]))
                .volume(new BigDecimal(split[8]))
                .amount(new BigDecimal(split[9]))
                .openTime(openTime)
                .closeTime(closeTime)
                .timestamp(timestamp)
                .orderBook(OrderBook.parseOrderBook(symbol, timestamp , lineText))
                .build();

    }

    private String retryRequestByUrl(String apiUrl) throws Exception {
        int retryCount = 3;
        for (int i = 0; i < retryCount; i++) {
            try {
                return requestByUrl(apiUrl);
            } catch (Exception ex) {
                log.error("error Sina requestByUrl...apiUrl: {}", apiUrl, ex);
                TimeUnit.SECONDS.sleep(10);
            }
        }
        return null;
    }

    private String requestByUrl(String apiUrl) throws InterruptedException, IOException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Referer", "https://vip.stock.finance.sina.com.cn/mkt/")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("error Sina requestByUrl, status code:{}, errMsg:\n{}", response.statusCode(), response.body());
            return null;
        }

        return response.body();

    }

    private String getApiUrl(String exchange, int page) {
        return sinaPageUrl + exchange + "&page=" + page;
    }


}
