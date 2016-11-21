package com.chimbori.liteapps;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;
import com.eclipsesource.json.WriterConfig;

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
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test that validates that each Lite App contains all the required fields in manifest.json.
 * Invalid behavior that should be added to this test:
 * - Invalid localizations (Text not correctly found in any messages.json).
 * - Missing localizations (manifest.json references a string, but string is not found in manifest.json).
 * - Extra files that are not part of the expected structure.
 */
public class ValidatorTest {

  private static final String HEX_COLOR_REGEXP = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
  private static final Pattern HEX_COLOR_PATTERN = Pattern.compile(HEX_COLOR_REGEXP);

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
            // Use a stricter parser than {@code JSONObject}, so we can catch issues such as
            // extra commas after the last element.
            JsonValue manifest = Json.parse(FileUtils.readFully(new FileInputStream(file.toFile())));
            // Re-indent the <b>source file</b> by saving the JSON back to the same file.
            FileUtils.writeFile(file.toFile(), manifest.toString(WriterConfig.PRETTY_PRINT));
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

      // Test that the "manifest_url" field contains a valid URL.
      try {
        String manifestUrl = manifestJson.getString(JSONConstants.Fields.MANIFEST_URL);
        URL manifest = new URL(manifestUrl);
        assertEquals("https", manifest.getProtocol());
        assertEquals("hermit.chimbori.com", manifest.getHost());
        assertTrue(manifest.getPath().startsWith("/lite-apps/"));
        assertTrue(manifest.getPath().endsWith(".hermit"));
        assertEquals(liteApp.getName() + ".hermit", new File(URLDecoder.decode(manifest.getFile())).getName());
      } catch (JSONException | MalformedURLException e) {
        fail(e.getMessage());
      }

      // Test that colors are valid hex colors.
      assertTrue(HEX_COLOR_PATTERN.matcher(manifestJson.getString(JSONConstants.Fields.THEME_COLOR)).matches());
      assertTrue(HEX_COLOR_PATTERN.matcher(manifestJson.getString(JSONConstants.Fields.SECONDARY_COLOR)).matches());

      // Test that the name of the icon file is "icon.png" & that the file exists.
      // Although any filename should work, having it be consistent in the library can let us
      // avoid a filename lookup in automated tests and refactors.
      assertEquals(FileUtils.ICON_FILENAME, manifestJson.getJSONArray(JSONConstants.Fields.ICONS).getJSONObject(0).getString(JSONConstants.Fields.SRC));
      assertTrue(new File(liteApp, FileUtils.ICON_FILENAME).exists());

      // Test "related_apps" for basic sanity, that if one exists, then it’s pointing to a Play Store app.
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

      // Test that if any localization files are present, then they are well-formed.
      File localesDirectory = new File(liteApp, FileUtils.LOCALES_DIR_NAME);
      if (localesDirectory.exists()) {
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
