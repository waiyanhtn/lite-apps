package com.chimbori.liteapps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

class LibraryGenerator {
  private static final int LIBRARY_ICON_SIZE = 112;

  /**
   * Individual manifest.json files do not contain any information about the organization of the
   * Lite Apps in the Library (e.g. categories, order within category, whether it should be
   * displayed or not. This metadata is stored in a separate index.json file. To minimize
   * duplication & to preserve a single source of truth, this file does not contain actual URLs
   * or anything about a Lite App other than its name (same as the directory name).
   * <p>
   * This generator tool combines the basic organizational metadata from index.json & detailed
   * Lite Apps data from * / manifest.json files. It outputs bin/lite-apps.json,
   * which is used as the basis for generating the Hermit Library page at
   * https://hermit.chimbori.com/library.
   */
  public static boolean generateLibraryData() throws IOException, JSONException {
    JSONArray library = new JSONArray(FileUtils.readFully(new FileInputStream(FileUtils.SRC_INDEX_JSON)));
    for (int i = 0; i < library.length(); i++) {
      JSONObject category = library.getJSONObject(i);
      String categoryName = category.getString(JSONConstants.Fields.CATEGORY);
      if (categoryName == null) {
        return false;
      }

      // For each Lite App in Category:
      JSONArray apps = category.getJSONArray(JSONConstants.Fields.APPS);
      for (int j = 0; j < apps.length(); j++) {
        JSONObject app = apps.getJSONObject(j);
        String appName = app.getString(JSONConstants.Fields.NAME);

        File liteAppDirectory = new File(FileUtils.SRC_ROOT_DIR, appName);
        if (liteAppDirectory.exists()) {
          File manifestJson = new File(liteAppDirectory, FileUtils.MANIFEST_JSON_FILE_NAME);
          if (manifestJson.exists()) {
            // Add manifest entry for this Lite App to the directory index file.
            JSONObject manifest = new JSONObject(FileUtils.readFully(new FileInputStream(manifestJson)));
            app.put(JSONConstants.Fields.URL, manifest.optString(JSONConstants.Fields.START_URL));
            app.put(JSONConstants.Fields.APP, String.format("%s.hermit", appName));
            app.put(JSONConstants.Fields.THEME_COLOR, manifest.optString(JSONConstants.Fields.THEME_COLOR));

            JSONObject settings = manifest.optJSONObject(JSONConstants.Fields.SETTINGS);
            if (settings != null &&
                settings.optString(JSONConstants.Fields.USER_AGENT, "").equals(JSONConstants.Values.USER_AGENT_DESKTOP)) {
              app.put(JSONConstants.Fields.USER_AGENT, JSONConstants.Values.USER_AGENT_DESKTOP);
            }

            // Resize the icon to be suitable for the Web, and copy it to the Web-accessible icons directory.
            resizeImage(
                new File(liteAppDirectory, FileUtils.ICON_FILENAME),
                new File(FileUtils.OUT_LIBRARY_ICONS_DIR, appName + FileUtils.ICON_EXTENSION),
                LIBRARY_ICON_SIZE, LIBRARY_ICON_SIZE);
          }
        }
      }
    }

    try (PrintWriter writer = new PrintWriter(FileUtils.OUT_LITE_APPS_JSON)) {
      writer.print(library.toString());
    }
    return true;
  }

  public static void resizeImage(File input, File output, int scaledWidth, int scaledHeight) throws IOException {
    BufferedImage inputImage = null;
    try {
      inputImage = ImageIO.read(input);
      BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

      Graphics2D g2d = outputImage.createGraphics();
      g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
      g2d.dispose();

      ImageIO.write(outputImage, FileUtils.getExtension(output), output);
    } catch (IOException | IllegalArgumentException e) {
      // Wrap in an IOException that forces callers to handle it, and insert a more detailed
      // error message.
      throw new IOException(
          String.format("Problem resizing [%s]", input.getAbsolutePath()), e);
    }
  }
}