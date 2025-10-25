package com.digcoo.fitech.common.model;


import com.digcoo.fitech.common.util.http.SinaHttpClient;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Data
public class OrderBook implements Serializable {
    private String symbol;
    private long lastUpdateId;

    @Getter
    private final ConcurrentNavigableMap<BigDecimal, BigDecimal> bidPriceToSizeMap = new ConcurrentSkipListMap<>(Comparator.reverseOrder()); // 买盘, 从大到小排列

    @Getter
    private final ConcurrentNavigableMap<BigDecimal, BigDecimal> askPriceToSizeMap = new ConcurrentSkipListMap<>(); // 卖盘，从小到大排列

    public static OrderBook parseOrderBook(String symbol, long timestamp, String lineText) {

        if (StringUtils.isNotBlank(lineText) && !lineText.equals("null")) {

            String lineData = lineText.substring(lineText.indexOf("=") + 2, lineText.lastIndexOf(";"));
            String[] split = lineData.split(",");

            OrderBook orderBook = new OrderBook();
            //bids
            orderBook.getBidPriceToSizeMap().put(new BigDecimal(split[11]), new BigDecimal(split[10]));
            orderBook.getBidPriceToSizeMap().put(new BigDecimal(split[13]), new BigDecimal(split[12]));
            orderBook.getBidPriceToSizeMap().put(new BigDecimal(split[15]), new BigDecimal(split[14]));
            orderBook.getBidPriceToSizeMap().put(new BigDecimal(split[17]), new BigDecimal(split[16]));
            orderBook.getBidPriceToSizeMap().put(new BigDecimal(split[19]), new BigDecimal(split[18]));

            //asks
            orderBook.getAskPriceToSizeMap().put(new BigDecimal(split[21]), new BigDecimal(split[20]));
            orderBook.getAskPriceToSizeMap().put(new BigDecimal(split[23]), new BigDecimal(split[22]));
            orderBook.getAskPriceToSizeMap().put(new BigDecimal(split[25]), new BigDecimal(split[24]));
            orderBook.getAskPriceToSizeMap().put(new BigDecimal(split[27]), new BigDecimal(split[26]));
            orderBook.getAskPriceToSizeMap().put(new BigDecimal(split[29]), new BigDecimal(split[28]));

            orderBook.setSymbol(symbol);
            orderBook.setLastUpdateId(timestamp);
            return orderBook;

        }

        return null;
    }

}
