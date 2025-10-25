package com.binance.client.utils;


import com.alibaba.fastjson.JSON;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class MessageSenderUtil {
    final static String url_stub = "http://wxpusher.zjiecode.com/api/send/message";
    final static String APP_TOKEN = "AT_WAqUI14umKb0DhVKMTvm9h9y0Di9ysAL";
    final static String UID = "UID_bU0BVWqoo2LMKH6gw5MeS0X0o7eA";
    public static void send(String symbol) throws IOException {
        Map data = new HashMap();
        data.put("appToken", APP_TOKEN);
        data.put("uids", Arrays.asList(UID));
        data.put("content", symbol);
        data.put("contentType", 1);
        data.put("summary", symbol);
        Request.Post(url_stub).bodyString(JSON.toJSONString(data), ContentType.APPLICATION_JSON).execute();
    }

    public static void main(String [] args) throws IOException {
        send("BTCUSDT开多");
    }
}
