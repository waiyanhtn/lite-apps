package com.chimbori.liteapps;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
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
  public void testAllManifestsAreValid() {
    File[] liteApps = FileUtils.SRC_ROOT_DIR.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });

    for (File liteApp : liteApps) {
      System.out.println(liteApp.getName());
      new JsonValidator(liteApp.getName(), new File(liteApp, "manifest.json"))
          .assertFieldExists(JSONFields.NAME)
          .assertFieldExists(JSONFields.START_URL)
          .assertFieldExists(JSONFields.LANG)
          .assertFieldExists(JSONFields.MANIFEST_URL)
          .assertFieldExists(JSONFields.THEME_COLOR)
          .assertFieldExists(JSONFields.SECONDARY_COLOR)
          .assertFieldExists(JSONFields.MANIFEST_VERSION)
          .assertFieldExists(JSONFields.ICONS);

      File localesDirectory = new File(liteApp, FileUtils.LOCALES_DIR_NAME);
      if (localesDirectory != null && localesDirectory.exists()) {
        File[] localizations = localesDirectory.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.isDirectory();
          }
        });
        for (File localization : localizations) {
          File messagesFile = new File(localization, FileUtils.MESSAGES_FILE_NAME);
          // With no specific field checks, we at least validate that the file is well-formed JSON.
          new JsonValidator(String.format("%s [%s]", liteApp.getName(), localization.getName()), messagesFile);
        }
      }
      System.out.println();
    }
  }

  private static JSONObject fromFile(File file) throws IOException, JSONException {
    if (!file.exists()) {
      return null;
    }
    return new JSONObject(FileUtils.readFully(new FileInputStream(file)));
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
        this.json = fromFile(file);
        System.out.println(String.format("- %s", this.tag));
      } catch (IOException | JSONException e) {
        fail(String.format("Invalid JSON: %s", tag));
      }
    }

    public JsonValidator assertFieldExists(String field) {
      assertNotNull(String.format("File [%s] is missing the field [%s]", tag, field), json.optString(field, null));
      return this;
    }
  }
}
