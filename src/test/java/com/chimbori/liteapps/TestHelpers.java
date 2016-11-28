package com.chimbori.liteapps;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestHelpers {
  public static void assertIsURL(String url) {
    try {
      new URL(url);
    } catch (MalformedURLException e) {
      fail(url);
    }
  }

  public static void assertIsNotEmpty(String string) {
    assertTrue(!string.isEmpty());
  }
}
