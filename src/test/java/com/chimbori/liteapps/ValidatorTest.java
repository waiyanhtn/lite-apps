package com.chimbori.liteapps;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.ParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test that validates that each Lite App contains all the required fields in manifest.json.
 * Invalid behavior that should be added to this test:
 * - Missing icons
 * - Colors are properly-formatted hex values.
 * - Invalid localizations (Text not correctly found in any messages.json).
 * - Missing localizations (manifest.json references a string, but string is not found in manifest.json).
 * - Extra files that are not part of the expected structure.
 */
public class ValidatorTest {
  @Before
  public void setUp() {
    FileUtils.OUT_ROOT_DIR.delete();
  }

  @Test
  public void testParseJSONStrictlyAndCheckWellFormed() throws IOException {
    Files.walkFileTree(FileUtils.SRC_ROOT_DIR.toPath(), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() && file.toFile().getName().endsWith(".json")) {
          try {
            Json.parse(FileUtils.readFully(new FileInputStream(file.toFile())));
          } catch (ParseException e) {
            fail(String.format("%s: %s", file.toFile().getPath(), e.getMessage()));
          }
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
        fail(e.getMessage());
        return FileVisitResult.CONTINUE;
      }
    });
  }

  @Test
  public void testAllManifestsAreValid() {
    File[] liteApps = FileUtils.SRC_ROOT_DIR.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });

    for (File liteApp : liteApps) {
      System.out.println(liteApp.getName());
      JsonValidator manifestJsonValidator = new JsonValidator(liteApp.getName(), new File(liteApp, FileUtils.MANIFEST_JSON_FILE_NAME))
          .assertFieldExists(JSONConstants.Fields.NAME)
          .assertFieldExists(JSONConstants.Fields.START_URL)
          .assertFieldExists(JSONConstants.Fields.LANG)
          .assertFieldExists(JSONConstants.Fields.MANIFEST_URL)
          .assertFieldExists(JSONConstants.Fields.THEME_COLOR)
          .assertFieldExists(JSONConstants.Fields.SECONDARY_COLOR)
          .assertFieldExists(JSONConstants.Fields.MANIFEST_VERSION)
          .assertFieldExists(JSONConstants.Fields.ICONS);

      JSONObject manifestJson = manifestJsonValidator.getJSON();
      try {
        String manifestUrl = manifestJson.getString(JSONConstants.Fields.MANIFEST_URL);
        URL manifest = new URL(manifestUrl);
      } catch (JSONException | MalformedURLException e) {
        fail(e.getMessage());
      }

      try {
        JSONArray relatedApps = manifestJson.optJSONArray(JSONConstants.Fields.RELATED_APPLICATIONS);
        if (relatedApps != null) {
          for (int i = 0; i < relatedApps.length(); i++) {
            JSONObject relatedApp = relatedApps.getJSONObject(i);
            assertEquals(JSONConstants.Values.PLAY, relatedApp.getString(JSONConstants.Fields.PLATFORM));

            String appId = relatedApp.getString(JSONConstants.Fields.ID);
            assertFalse(appId.isEmpty());

            String appUrl = relatedApp.getString(JSONConstants.Fields.URL);
            assertTrue(appUrl.endsWith(appId));
            assertTrue(appUrl.startsWith("https://play.google.com/store/apps/details?id="));
          }
        }

      } catch (JSONException e) {
        fail(e.getMessage());
      }

      File localesDirectory = new File(liteApp, FileUtils.LOCALES_DIR_NAME);
      if (localesDirectory != null && localesDirectory.exists()) {
        File[] localizations = localesDirectory.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.isDirectory();
          }
        });
        for (File localization : localizations) {
          File messagesFile = new File(localization, FileUtils.MESSAGES_JSON_FILE_NAME);
          // With no specific field checks, we at least validate that the file is well-formed JSON.
          new JsonValidator(String.format("%s [%s]", liteApp.getName(), localization.getName()), messagesFile);
        }
      }
      System.out.println();
    }
  }

  /**
   * Loads a JSON file and can perform multiple validations on it.
   */
  private static class JsonValidator {
    private final String tag;
    private JSONObject json;

    public JsonValidator(String tag, File file) {
      if (file == null || !file.exists()) {
        fail("Not found: " + file.getAbsolutePath());
      }

      this.tag = tag;
      try {
        this.json = file.exists() ? new JSONObject(FileUtils.readFully(new FileInputStream(file))) : null;
        System.out.println(String.format("- %s", this.tag));
      } catch (IOException | JSONException e) {
        fail(String.format("Invalid JSON: %s", tag));
      }
    }

    public JsonValidator assertFieldExists(String field) {
      assertNotNull(String.format("File [%s] is missing the field [%s]", tag, field), json.optString(field, null));
      return this;
    }

    public JSONObject getJSON() {
      return json;
    }
  }
}
