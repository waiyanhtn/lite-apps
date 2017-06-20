package com.chimbori.liteapps;

import net.coobird.thumbnailator.Thumbnails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

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
    JSONArray inputLibrary = new JSONArray(FileUtils.readFully(new FileInputStream(FileUtils.SRC_INDEX_JSON)));
    JSONArray outputLibrary = new JSONArray();

    for (int i = 0; i < inputLibrary.length(); i++) {
      JSONObject inputCategory = inputLibrary.getJSONObject(i);
      String categoryName = inputCategory.getString(JSONConstants.Fields.CATEGORY);
      if (categoryName == null) {
        return false;
      }

      JSONObject outputCategory = new JSONObject();
      outputCategory.put(JSONConstants.Fields.CATEGORY, categoryName);

      // For each Lite App in Category:
      JSONArray inputApps = inputCategory.getJSONArray(JSONConstants.Fields.APPS);
      JSONArray outputApps = new JSONArray();

      for (int j = 0; j < inputApps.length(); j++) {
        JSONObject inputApp = inputApps.getJSONObject(j);
        // If this entry has an explicit "display":false, then skip to the next one.
        if (!inputApp.optBoolean(JSONConstants.Fields.DISPLAY, true /* defaultValue */)) {
          continue;
        }

        JSONObject outputApp = new JSONObject();
        String appName = inputApp.getString(JSONConstants.Fields.NAME);
        outputApp.put(JSONConstants.Fields.NAME, appName);

        File liteAppDirectory = new File(FileUtils.SRC_ROOT_DIR, appName);
        if (liteAppDirectory.exists()) {
          File manifestJson = new File(liteAppDirectory, FileUtils.MANIFEST_JSON_FILE_NAME);
          if (manifestJson.exists()) {
            // Add manifest entry for this Lite App to the directory index file.
            JSONObject manifest = new JSONObject(FileUtils.readFully(new FileInputStream(manifestJson)));
            outputApp.put(JSONConstants.Fields.URL, manifest.optString(JSONConstants.Fields.START_URL));
            outputApp.put(JSONConstants.Fields.APP, String.format("%s.hermit", appName));
            outputApp.put(JSONConstants.Fields.THEME_COLOR, manifest.optString(JSONConstants.Fields.THEME_COLOR));

            JSONObject settings = manifest.optJSONObject(JSONConstants.Fields.SETTINGS);
            if (settings != null &&
                settings.optString(JSONConstants.Fields.USER_AGENT, "").equals(JSONConstants.Values.USER_AGENT_DESKTOP)) {
              outputApp.put(JSONConstants.Fields.USER_AGENT, JSONConstants.Values.USER_AGENT_DESKTOP);
            }

            outputApps.put(outputApp);

            // Resize the icon to be suitable for the Web, and copy it to the Web-accessible icons directory.
            Thumbnails.of(new File(liteAppDirectory, FileUtils.ICON_FILENAME))
                .outputQuality(1.0f)
                .useOriginalFormat()
                .size(LIBRARY_ICON_SIZE, LIBRARY_ICON_SIZE)
                .imageType(BufferedImage.TYPE_INT_ARGB)
                .toFile(new File(FileUtils.OUT_LIBRARY_ICONS_DIR, appName + FileUtils.ICON_EXTENSION));
          }
        }
      }

      outputCategory.put(JSONConstants.Fields.APPS, outputApps);

      outputLibrary.put(outputCategory);
    }

    try (PrintWriter writer = new PrintWriter(FileUtils.OUT_LITE_APPS_JSON)) {
      writer.print(outputLibrary.toString());
    }
    return true;
  }
}