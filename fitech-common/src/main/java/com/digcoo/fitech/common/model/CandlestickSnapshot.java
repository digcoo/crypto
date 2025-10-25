package com.digcoo.fitech.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandlestickSnapshot implements Serializable {
    private String symbol;
    private Map<String, List<Candlestick>> candlesticksMap = new ConcurrentHashMap<>();
}
