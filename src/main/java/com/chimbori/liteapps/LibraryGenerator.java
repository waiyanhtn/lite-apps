package com.chimbori.liteapps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class LibraryGenerator {
  /**
   * The lite-apps.json file (containing metadata about all Lite Apps) is used as the basis for
   * generating the Hermit Library page at https://hermit.chimbori.com/library.
   * <p>
   * It is currently the source of truth, except for the "app" field, which is not yet included
   * in the hand-written file. This method looks at all the generated files, and if one exists, then
   * it adds an "app" JSON field pointing to the filename of the "*.hermit" file.
   * <p>
   * Eventually, this source file should go away, and the directory of lite apps should become the
   * source of truth. This file will then be completely generated (not hand-written), and will
   * continue to be used to build the Hermit Library Web UI.
   */
  public static boolean generateLibraryData() throws IOException, JSONException {
    JSONArray library = new JSONArray(FileUtils.readFully(new FileInputStream(FileUtils.SRC_LITE_APPS_JSON)));
    for (int i = 0; i < library.length(); i++) {
      JSONObject category = library.getJSONObject(i);
      String categoryName = category.getString(JSONConstants.Fields.CATEGORY);
      if (categoryName == null) {
        return false;
      }

      JSONArray apps = category.getJSONArray(JSONConstants.Fields.APPS);
      for (int j = 0; j < apps.length(); j++) {
        JSONObject app = apps.getJSONObject(j);
        String appName = app.getString(JSONConstants.Fields.NAME);
        // Check if the corresponding *.hermit file exists before adding the "app" field.
        if (FileUtils.packagedLiteAppExists(appName)) {
          app.put(JSONConstants.Fields.APP, String.format("%s.hermit", appName));
        }
      }
    }

    try (PrintWriter writer = new PrintWriter(FileUtils.OUT_LITE_APPS_JSON)) {
      writer.print(library.toString(2));
    }
    return true;
  }
}
