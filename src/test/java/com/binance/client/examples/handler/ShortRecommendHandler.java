package com.binance.client.examples.handler;

import com.alibaba.fastjson.JSON;
import com.binance.client.examples.KLine;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ShortRecommendHandler implements HttpHandler {

    List<KLine> targetList = null;

    public ShortRecommendHandler(List<KLine> targetList) {
        this.targetList = targetList;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        try {

            List<KLine> tmpKlines = new ArrayList();
            tmpKlines.addAll(targetList);

            Collections.sort(tmpKlines, new Comparator<KLine>() {
                @Override
                public int compare(KLine p1, KLine p2) {
                    return p1.getM30Lines().get(p1.getM30Lines().size() - 1).getQuoteAssetVolume().compareTo(p2.getM30Lines().get(p2.getM30Lines().size() - 1).getQuoteAssetVolume()) >= 0? -1 : 1;
                }
            });

            List<KLine> newTargetList = new ArrayList<>();
            for (KLine kLine : tmpKlines) {
                newTargetList.add(new KLine(kLine.getCode()));
            }

            newTargetList = newTargetList.subList(0, Math.min(newTargetList.size(), 30));
            response = JSON.toJSONString(newTargetList);

        }catch (Exception ex) {
            Map res = new HashMap();
            res.put("code", "500");
            response = JSON.toJSONString(res);

            ex.printStackTrace();
        }

        // 设置响应头部信息
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // 允许所有来源请求
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS"); // 允许的请求方法

        // 发送响应正文
        exchange.sendResponseHeaders(200, response.length());
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.close();
    }


    public Map<String, String> getParams(HttpExchange exchange) {
        Map<String, String> queryParams = new HashMap<>();
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            String[] queryParamsArr = query.split("&");
            for (String param : queryParamsArr) {
                String[] keyValue = param.split("=");
                String key = keyValue[0];
                String value = "";
                if (keyValue.length > 1) {
                    value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                }
                queryParams.put(key, value);
            }
        }

        return queryParams;
    }

}
