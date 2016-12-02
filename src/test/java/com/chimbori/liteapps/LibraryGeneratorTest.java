package com.chimbori.liteapps;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Generates the library JSON file.
 */
public class LibraryGeneratorTest {
  @Test
  public void testIndexJsonIsWellFormedAndReformat() throws IOException {
    TestHelpers.assertJsonIsWellFormedAndReformat(FileUtils.SRC_INDEX_JSON);
  }

  @Test
  public void testLibraryDataIsGeneratedSuccessfully() {
    try {
      assertTrue(LibraryGenerator.generateLibraryData());
    } catch (IOException | JSONException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testThatEveryManifestIsIncludedInIndexJsonFile() throws IOException {
    Set<String> allLiteApps = new HashSet<>();

    // Add all Lite Apps to a set.
    File[] files = FileUtils.SRC_ROOT_DIR.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        allLiteApps.add(file.getName());
      }
    }

    // Remove from the set if the Lite App is found in the index.json file.
    JSONArray library = new JSONArray(FileUtils.readFully(new FileInputStream(FileUtils.SRC_INDEX_JSON)));
    for (int i = 0; i < library.length(); i++) {
      JSONArray apps = library.getJSONObject(i).getJSONArray(JSONConstants.Fields.APPS);
      for (int j = 0; j < apps.length(); j++) {
        JSONObject app = apps.getJSONObject(j);
        String appName = app.getString(JSONConstants.Fields.NAME);
        allLiteApps.remove(appName);
      }
    }

    // Assert that there are now no more Lite Apps left.
    assertEquals("Lite Apps not found in index.json: " + allLiteApps.toString(), 0, allLiteApps.size());
  }

  @Test
  public void testThatEveryLiteAppInIndexJsonHasAManifestJson() throws IOException {
    JSONArray library = new JSONArray(FileUtils.readFully(new FileInputStream(FileUtils.SRC_INDEX_JSON)));
    for (int i = 0; i < library.length(); i++) {
      JSONArray apps = library.getJSONObject(i).getJSONArray(JSONConstants.Fields.APPS);
      for (int j = 0; j < apps.length(); j++) {
        JSONObject app = apps.getJSONObject(j);
        String appName = app.getString(JSONConstants.Fields.NAME);

        File liteAppDirectory = new File(FileUtils.SRC_ROOT_DIR, appName);
        assertTrue(liteAppDirectory.exists());

        File manifestJson = new File(liteAppDirectory, FileUtils.MANIFEST_JSON_FILE_NAME);
        assertTrue(manifestJson.exists());
      }
    }
  }
}
