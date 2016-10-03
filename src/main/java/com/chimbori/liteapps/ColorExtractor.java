package com.chimbori.liteapps;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Extracts the dominant color from an image using a simple fast algorithm. It only considers
 * exact matches for color, so if a JPEG contains a small area of homogenous color and a large area
 * of almost-homogenous color, the smaller area’s color will be reported. Since we use this mostly
 * to extract color from flat-color icons, it works well in practice for our dataset.
 */
class ColorExtractor {
  public static class Color {
    private static int TOLERANCE_FOR_GREY = 16;

    private final int r;
    private final int g;
    private final int b;

    Color(int r, int g, int b) {
      this.r = r;
      this.g = g;
      this.b = b;
    }

    static Color from(int rgba) {
      return new Color((rgba >> 16) & 0xff, (rgba >> 8) & 0xff, rgba & 0xff);
    }

    boolean isGrey() {
      return (Math.abs(r - b) <= TOLERANCE_FOR_GREY && Math.abs(r - g) <= TOLERANCE_FOR_GREY);
    }

    Color darken(float ratio) {
      if (ratio < 0 || ratio >= 1) {
        throw new IllegalArgumentException();
      }
      return new Color((int) (r * ratio), (int) (g * ratio), (int) (b * ratio));
    }

    @Override
    public String toString() {
      return String.format("#%02x%02x%02x", r, g, b);
    }
  }

  static Color getDominantColor(BufferedImage image) {
    Map<Color, Integer> colorFrequencies = new HashMap<>();
    int height = image.getHeight();
    int width = image.getWidth();

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        Color pixel = Color.from(image.getRGB(i, j));
        if (!pixel.isGrey()) {
          Integer counter = colorFrequencies.get(pixel);
          if (counter == null) {
            counter = 0;
          }
          colorFrequencies.put(pixel, ++counter);
        }
      }
    }
    return getDominantColor(colorFrequencies);
  }

  private static Color getDominantColor(Map<Color, Integer> map) {
    List<Map.Entry<Color, Integer>> list = new LinkedList<>(map.entrySet());

    Collections.sort(list, (Map.Entry<Color, Integer> obj1, Map.Entry<Color, Integer> obj2)
        -> ((Comparable) obj1.getValue()).compareTo(obj2.getValue()));

    return (list.size() > 0)
        ? list.get(list.size() - 1).getKey()
        : new Color(0, 0, 0);  // Defaults to black.
  }
}
