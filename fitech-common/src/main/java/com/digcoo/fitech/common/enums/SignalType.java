package com.digcoo.fitech.common.enums;

/**
 * buy, sell.
 */

public enum SignalType {
  BUY("BUY"),
  SELL("SELL"),
  NO_SIGNAL("NO_SIGNAL");

  private final String code;

  SignalType(String side) {
    this.code = side;
  }

  @Override
  public String toString() {
    return code;
  }


}