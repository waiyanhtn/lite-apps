package com.chimbori.liteapps;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;
import com.eclipsesource.json.WriterConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

class TestHelpers {
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

  static void assertJsonIsWellFormedAndReformat(File file) throws IOException {
    try {
      // Use a stricter parser than {@code JSONObject}, so we can catch issues such as
      // extra commas after the last element.
      JsonValue manifest = Json.parse(FileUtils.readFully(new FileInputStream(file)));
      // Re-indent the <b>source file</b> by saving the JSON back to the same file.
      FileUtils.writeFile(file, manifest.toString(WriterConfig.PRETTY_PRINT));
    } catch (ParseException e) {
      fail(String.format("%s: %s", file.getPath(), e.getMessage()));
    }
  }
}
