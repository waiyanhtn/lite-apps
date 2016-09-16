package com.chimbori.liteapps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Packages all Lite Apps into their corresponding .hermit packages.
 * Does not validate each Lite App prior to packaging: it is assumed that this is run on a green
 * build which has already passed all validation tests.
 */
public class Packager {

  public static void main(String[] arguments) {
    boolean allSuccessful = true;
    allSuccessful &= packageAllManifests(FileUtils.SRC_ROOT_DIR);
    try {
      allSuccessful &= generateLibraryData();
    } catch (IOException | JSONException e) {
      allSuccessful = false;
      e.printStackTrace();
    }

    // Indicate to the shell that processing failed.
    if (!allSuccessful) {
      System.exit(1);
    }
  }

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
      System.out.println(categoryName);

      JSONArray apps = category.getJSONArray(JSONConstants.Fields.APPS);
      for (int j = 0; j < apps.length(); j++) {
        JSONObject app = apps.getJSONObject(j);
        System.out.println(String.format("- %s", app.getString(JSONConstants.Fields.NAME)));
        String appName = app.getString(JSONConstants.Fields.NAME);
        // Check if the corresponding *.hermit file exists before adding the "app" field.
        // If the source Lite App still has the UNDER_DEVELOPMENT field present, then a *.hermit
        // file will not be produced for it, and thus, no additional checks are needed here.
        if (FileUtils.packagedLiteAppExists(appName)) {
          app.put(JSONConstants.Fields.APP, String.format("%s.hermit", appName));
        }
      }
      System.out.println();
    }

    try (PrintWriter writer = new PrintWriter(FileUtils.OUT_LITE_APPS_JSON)) {
      writer.print(library.toString(2));
    }
    return true;
  }

  /**
   * Packages all the manifests from source directories and individual files into a zipped file
   * and places it in the correct location.
   */
  public static boolean packageAllManifests(File liteAppsDirectory) {
    boolean allSuccessful = true;
    for (File liteApp : liteAppsDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    })) {
      allSuccessful = allSuccessful && packageManifest(liteApp);
    }
    return allSuccessful;
  }

  /**
   * Packages a single manifest from a source directory & individual files into a zipped file and
   * places it in the correct location.
   */
  private static boolean packageManifest(File liteAppDirectory) {
    File liteAppZipped = new File(FileUtils.OUT_LITE_APPS_DIR, String.format("%s.hermit", liteAppDirectory.getName()));

    JSONObject manifestJson;
    try {
      manifestJson = new JSONObject(FileUtils.readFully(new FileInputStream(
          new File(liteAppDirectory, FileUtils.MANIFEST_JSON_FILE_NAME))));
    } catch (JSONException | IOException e) {
      return false;
    }

    if (manifestJson != null && manifestJson.has(JSONConstants.Fields.UNDER_DEVELOPMENT)) {
      // Skip generating the zip file for Lite Apps not yet manually vetted, but this isn’t an error, so return true.
      return true;
    }

    System.out.println(liteAppZipped.getName());
    FileUtils.zip(liteAppDirectory, liteAppZipped);
    System.out.println();
    return true;
  }
}
