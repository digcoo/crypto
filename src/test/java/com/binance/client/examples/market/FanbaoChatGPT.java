//package com.binance.client.examples.market;
//
//import com.binance.client.RequestOptions;
//import com.binance.client.SyncRequestClient;
//import com.binance.client.examples.constants.PrivateConfig;
//import com.binance.client.model.enums.CandlestickInterval;
//import com.binance.client.model.market.Candlestick;
//import com.binance.client.model.market.ExchangeInformation;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//public class FanbaoChatGPT {
//
//    private static final int POLLING_INTERVAL = 60_000; // 每1分钟轮询一次
//    private static final int KLINE_SIZE = 5; // 每5分钟打印一次
//    private static final Map<String, Kline> klineMap = new HashMap<>();
//
//    public static void main(String[] args) throws InterruptedException {
//
//        RequestOptions options = new RequestOptions();
//        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
//                options);
//
//        // 获取所有交易对
//        ExchangeInformation exchangeInformation = syncRequestClient.getExchangeInformation();
//        List<String> symbols = exchangeInformation.getSymbols().stream()
//                .filter(x -> x.getSymbol().endsWith("USDT"))
//                .map(x -> x.getSymbol())
//                .collect(Collectors.toList());
//
//        // 初始化K线数据
//        for (String symbol : symbols) {
//            klineMap.put(symbol, new Kline());
//        }
//
//        // 开始轮询
//        while (true) {
//            // 获取最新的K线数据
////            List<Candlestick> candlesticks = syncRequestClient.getCandlestick(symbol, CandlestickInterval.FIVE_MINUTES, null, null, 2);
//            List<Candlestick> candlesticks = syncRequestClient.getAllCandlestickBars(CandlestickInterval.ONE_MINUTE);
//
//            // 更新K线数据
//            for (Candlestick candlestick : candlesticks) {
//                Kline kline = klineMap.get(candlestick.getSymbol());
//                kline.update(candlestick);
//            }
//
//            // 检查是否要打印K线反包
//            for (String symbol : klineMap.keySet()) {
//                Kline kline = klineMap.get(symbol);
//                if (kline.isReady() && kline.isReversePackage()) {
//                    System.out.println(symbol + " 完成K线反包，前一根K线：\n" + kline.getPreviousCandlestick() + "当前K线：\n" + kline.getCurrentCandlestick());
//                    kline.reset();
//                }
//            }
//
//            // 等待1分钟后继续轮询
//            Thread.sleep(POLLING_INTERVAL);
//        }
//    }
//
//    private static class Kline {
//        private Candlestick previousCandlestick;
//        private Candlestick currentCandlestick;
//        private long timestamp;
//
//        public void update(Candlestick candlestick) {
//            if (candlestick.getOpenTime() > timestamp) {
//                previousCandlestick = currentCandlestick;
//                currentCandlestick = candlestick;
//                timestamp = candlestick.getOpenTime();
//            }
//        }
//
//        public boolean isReady() {
//            return previousCandlestick != null && currentCandlestick != null &&
//                    (currentCandlestick.getCloseTime() - previousCandlestick.getOpenTime()) == KLINE_SIZE * 60_000;
//        }
//
//        public boolean isReversePackage() {
//            if (previousCandlestick.getClose().compareTo(previousCandlestick.getOpen()) > 0) { // 前一根K线涨
//                return currentCandlestick.getClose().compareTo(currentCandlestick.getOpen()) < 0
//                        && currentCandlestick.getOpen().compareTo(previousCandlestick.getOpen()) > 0;
//            } else { // 前一根K线跌
//                return currentCandlestick.getClose().compareTo(currentCandlestick.getOpen()) > 0
//                        && currentCandlestick.getOpen().compareTo(previousCandlestick.getOpen()) < 0;
//            }
//        }
//
//        public Candlestick getPreviousCandlestick() {
//            return previousCandlestick;
//        }
//
//        public Candlestick getCurrentCandlestick() {
//            return currentCandlestick;
//        }
//
//        public void reset() {
//            previousCandlestick = null;
//            currentCandlestick = null;
//        }
//    }
//}
