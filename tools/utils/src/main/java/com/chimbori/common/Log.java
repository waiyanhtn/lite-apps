package com.chimbori.common;

public class Log {
  public static void i(String message, Object... parameters) {
    System.out.println(String.format(message, parameters));
  }
}
