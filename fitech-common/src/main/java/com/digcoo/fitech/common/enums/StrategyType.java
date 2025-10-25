package com.digcoo.fitech.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * buy, sell, both.
 */
@Getter
@AllArgsConstructor
public enum StrategyType {
  FAN_BAO("fb", "反包"),

  MACD_GOLD_BACK_CROSS("mgdc", "MACD黄金位--回踩"),

  MACD_DOWN_RISE("mdr", "MACD之下--超跌反弹"),

  MACD_BETWEEN_RISE("mbr", "MACD区间--趋势上涨"),

  MACD_OVER_RISE("mor", "MACD之上--趋势上涨"),

  MACD_OVER_TURN_ROUND("mot", "MACD之上--调整后向上"),

  ;

  private final String code;
  private final String desc;

}