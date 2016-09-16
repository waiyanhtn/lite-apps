package com.chimbori.liteapps;

import org.json.JSONException;
import org.json.JSONObject;
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
public class ManifestValidatorTest {
  @Test
  public void testAllManifestsAreValid() {
    File[] liteApps = new File("lite-apps/").listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    });

    for (File liteApp : liteApps) {
      System.out.println(liteApp.getName());
      new JsonValidator(liteApp.getName(), new File(liteApp, "manifest.json"))
          .assertFieldExists("name")
          .assertFieldExists("start_url")
          .assertFieldExists("lang")
          .assertFieldExists("manifest_url")
          .assertFieldExists("theme_color")
          .assertFieldExists("secondary_color")
          .assertFieldExists("manifest_version")
          .assertFieldExists("icons");

      File localesDirectory = new File(liteApp, "_locales");
      if (localesDirectory != null && localesDirectory.exists()) {
        File[] localizations = localesDirectory.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.isDirectory();
          }
        });
        for (File localization : localizations) {
          File messagesFile = new File(localization, "messages.json");
          // With no specific field checks, we at least validate that the file is well-formed JSON.
          new JsonValidator(String.format("%s/%s", liteApp.getName(), localization.getName()), messagesFile);
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

  private static class JsonValidator {
    private final String tag;
    private JSONObject json;

    public JsonValidator(String tag, File file) {
      this.tag = tag;
      try {
        this.json = fromFile(file);
        System.out.println(String.format("- %s", this.tag));
      } catch (IOException | JSONException e) {
        fail(String.format("Invalid JSON: %s", tag));
      }
    }

    public JsonValidator assertFieldExists(String field) {
      assertNotNull(String.format("File [%s] is missing the field [%s]", tag, field),
          json.optString(field, null));
      return this;
    }
  }
}
