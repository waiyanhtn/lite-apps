package com.chimbori.liteapps;

import com.chimbori.common.FileUtils;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;
import com.eclipsesource.json.WriterConfig;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

class TestHelpers {
  public static void assertIsURL(String message, String url) {
    try {
      new URL(url);
    } catch (MalformedURLException e) {
      fail(message);
    }
  }

  public static void assertIsNotEmpty(String message, String string) {
    assertTrue(message, !string.isEmpty());
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

  static void assertThatIconIs300x300(File icon) {
    BufferedImage bufferedImage = null;
    try {
      bufferedImage = ImageIO.read(icon);
    } catch (IOException e) {
      fail(String.format("%s: %s", icon.getPath(), e.getMessage()));
    }

    assertEquals(String.format("[%s] is not the correct size.", icon.getPath()), 300, bufferedImage.getWidth());
    assertEquals(String.format("[%s] is not the correct size.", icon.getPath()), 300, bufferedImage.getHeight());
  }
}
