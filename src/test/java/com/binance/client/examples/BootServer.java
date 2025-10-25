package com.binance.client.examples;

import com.alibaba.fastjson.JSON;
import com.binance.client.examples.handler.KlineHandler;
import com.binance.client.examples.handler.LongRecommendHandler;
import com.binance.client.examples.handler.ShortRecommendHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Slf4j
public class BootServer {

    static Map<String, KLine> klineMap = new ConcurrentHashMap<>();
    static List<KLine> longKLineList = new CopyOnWriteArrayList<>();
    static List<KLine> shortKLineList = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws Exception {
        // 创建HttpServer实例，监听8080端口
        HttpServer server = HttpServer.create(new InetSocketAddress(9090), 0);

        // 创建处理请求的上下文
        server.createContext("/future/klines", new KlineHandler(klineMap));

        // 创建处理请求的上下文(多头)
        server.createContext("/future/recommendLong", new LongRecommendHandler(longKLineList));
        // 创建处理请求的上下文(空头)
        server.createContext("/future/recommendShort", new ShortRecommendHandler(shortKLineList));

        // 设置服务器的线程池数量
        server.setExecutor(null); // 使用默认的线程池

        // 启动服务器
        server.start();

        System.out.println("Server started on port 9090");

        MyStrategyThread myStrategyThread = new MyStrategyThread(klineMap, longKLineList, shortKLineList);
        myStrategyThread.start();

    }
 }
