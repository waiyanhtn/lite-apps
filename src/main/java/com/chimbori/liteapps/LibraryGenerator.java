package com.chimbori.liteapps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

class LibraryGenerator {
  /**
   * Individual manifest.json files do not contain any information about the organization of the
   * Lite Apps in the Library (e.g. categories, order within category, whether it should be
   * displayed or not. This metadata is stored in a separate index.json file. To minimize
   * duplication & to preserve a single source of truth, this file does not contain actual URLs
   * or anything about a Lite App other than its name (same as the directory name).
   *
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
            JSONObject manifest = new JSONObject(FileUtils.readFully(new FileInputStream(manifestJson)));
            app.put(JSONConstants.Fields.URL, manifest.optString(JSONConstants.Fields.START_URL));
            app.put(JSONConstants.Fields.APP, String.format("%s.hermit", appName));
            app.put(JSONConstants.Fields.THEME_COLOR, manifest.optString(JSONConstants.Fields.THEME_COLOR));
          }
        }
      }
    }

    try (PrintWriter writer = new PrintWriter(FileUtils.OUT_LITE_APPS_JSON)) {
      writer.print(library.toString(2));
    }
    return true;
  }
}