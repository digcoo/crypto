package com.digcoo.fitech.common.enums;

/**
 * buy, sell.
 */

public enum TrendPeriodType {
  RECOVERY("复苏"),   //MACD红，价格在红实体区间（low - high）
    //机会：突破红黄金位底部


  PROSPERITY("繁荣"),   //MACD红，价格在红实体High上
    //机会：突破红黄金位顶部



  BUBBLE1("泡沫1"),     //MACD红，价格在两个黄金位High之上


  RECESSION1("假衰退"),     //MACD绿，价格在绿黄金位Low之上
    //机会：突破绿黄金位底部/顶部



  RECESSION2("衰退"),   //MACD绿，价格在绿黄金位Low之下


  DEPRESSION("萧条"),     //MACD绿，价格在两黄金位Low之下
  ;

  private final String code;

  TrendPeriodType(String side) {
    this.code = side;
  }

  @Override
  public String toString() {
    return code;
  }

  /**
   *  扩张期（Expansion）
   * @return
   */
  public boolean isExpansion() {
    return this == SPRING || this == SUMMER || this == AUTUMN || this == WINTER_STRUGGLE;
  }

  /**
   *  繁荣期/峰值（Peak）
   * @return
   */
  public boolean isPeak() {
    return this == SPRING || this == SUMMER || this == AUTUMN;
  }


  /**
   * 衰退期（Recession）
   * @return
   */
  public boolean isRecession() {
    return this == WINTER;
  }


  /**
   * 萧条期/谷底（Trough）
   * @return
   */
  public boolean isTrough() {
    return this == WINTER_STRUGGLE;
  }

}