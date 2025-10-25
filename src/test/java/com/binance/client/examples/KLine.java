package com.binance.client.examples;

import com.binance.client.model.market.Candlestick;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


//@Data
//@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class KLine {

    private String symbol;

    private List<Candlestick> mLines;
    private List<Candlestick> wLines;
    private List<Candlestick> dLines;
    private List<Candlestick> h4Lines;
    private List<Candlestick> m30Lines;
    private List<Candlestick> m5Lines;

    public KLine(String symbol, List<Candlestick> mLines, List<Candlestick> wLines, List<Candlestick> dLines, List<Candlestick> h4Lines, List<Candlestick> m30Lines, List<Candlestick> m5Lines) {
        this.symbol = symbol;
        this.mLines = mLines;
        this.wLines = wLines;
        this.dLines = dLines;
        this.h4Lines = h4Lines;
        this.m30Lines = m30Lines;
        this.m5Lines = m5Lines;
    }

    public String getCode() {
        return this.symbol;
    }

    public KLine(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public List<Candlestick> getMLines() {
        return mLines;
    }

    public List<Candlestick> getWLines() {
        return wLines;
    }

    public List<Candlestick> getDLines() {
        return dLines;
    }

    public List<Candlestick> getH4Lines() {
        return h4Lines;
    }

    public List<Candlestick> getM30Lines() {
        return m30Lines;
    }

    public List<Candlestick> getM5Lines() {
        return m5Lines;
    }
}
