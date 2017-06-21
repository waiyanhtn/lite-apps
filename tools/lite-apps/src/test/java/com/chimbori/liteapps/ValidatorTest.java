package com.chimbori.liteapps;

import com.chimbori.common.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import static com.chimbori.liteapps.TestHelpers.assertIsNotEmpty;
import static com.chimbori.liteapps.TestHelpers.assertIsURL;
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
@RunWith(Parameterized.class)
public class ValidatorTest extends ParameterizedLiteAppTest {
  private static final String HEX_COLOR_REGEXP = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$";
  private static final Pattern HEX_COLOR_PATTERN = Pattern.compile(HEX_COLOR_REGEXP);

  public ValidatorTest(File liteApp) {
    super(liteApp);
  }

  @Before
  public void setUp() {
    FilePaths.OUT_ROOT_DIR.delete();
  }

  @Test
  public void testManifestIsValid() {
    JsonValidatorHelper manifestJsonValidator = new JsonValidatorHelper(liteApp.getName(), new File(liteApp, FilePaths.MANIFEST_JSON_FILE_NAME))
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
    assertTrue(String.format("[%s] theme_color should be a valid hex color", liteApp.getName()),
        HEX_COLOR_PATTERN.matcher(manifestJson.getString(JSONConstants.Fields.THEME_COLOR)).matches());
    assertTrue(String.format("[%s] secondary_color should be a valid hex color", liteApp.getName()),
        HEX_COLOR_PATTERN.matcher(manifestJson.getString(JSONConstants.Fields.SECONDARY_COLOR)).matches());

    // Test that the name of the icon file is "icon.png" & that the file exists.
    // Although any filename should work, having it be consistent in the library can let us
    // avoid a filename lookup in automated tests and refactors.
    assertEquals(FilePaths.ICON_FILENAME, manifestJson.getJSONArray(JSONConstants.Fields.ICONS).getJSONObject(0).getString(JSONConstants.Fields.SRC));
    assertTrue(new File(liteApp, FilePaths.ICON_FILENAME).exists());

    // Test Endpoints for basic parseability.
    validateEndpoints(manifestJson, JSONConstants.Roles.FEEDS);
    validateEndpoints(manifestJson, JSONConstants.Roles.BOOKMARKS);
    validateEndpoints(manifestJson, JSONConstants.Roles.CREATE);
    validateEndpoints(manifestJson, JSONConstants.Roles.SHARE);
    validateEndpoints(manifestJson, JSONConstants.Roles.SEARCH);
    validateEndpoints(manifestJson, JSONConstants.Roles.MONITORS);

    // Test all Settings to see whether they belong to our whitelisted set of allowable strings.
    validateSettings(manifestJson);

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
    File localesDirectory = new File(liteApp, FilePaths.LOCALES_DIR_NAME);
    if (localesDirectory.exists()) {
      File[] localizations = localesDirectory.listFiles(File::isDirectory);
      for (File localization : localizations) {
        File messagesFile = new File(localization, FilePaths.MESSAGES_JSON_FILE_NAME);
        // With no specific field checks, we at least validate that the file is well-formed JSON.
        new JsonValidatorHelper(String.format("%s [%s]", liteApp.getName(), localization.getName()), messagesFile);
      }
    }
  }

  private void validateEndpoints(JSONObject manifestJson, String role) {
    final String LITE_APP_NAME = manifestJson.optString(JSONConstants.Fields.NAME);
    JSONArray feeds = manifestJson.optJSONArray(role);
    if (feeds != null) {
      for (int i = 0; i < feeds.length(); i++) {

        JSONObject feed = feeds.getJSONObject(i);
        String name = feed.optString(JSONConstants.Fields.NAME);
        assertIsNotEmpty("Endpoint name should not be empty: " + LITE_APP_NAME, name);
        String url = feed.optString(JSONConstants.Fields.URL);
        assertIsURL("Endpoint should have a valid URL: " + LITE_APP_NAME, url);

        if (JSONConstants.Roles.SEARCH.equals(role)) {
          assertTrue(url, url.contains("%s"));
        } else if (JSONConstants.Roles.SHARE.equals(role)) {
          assertTrue(url, url.contains("%s")
              || url.contains("%t")
              || url.contains("%u"));
        } else if (JSONConstants.Roles.MONITORS.equals(role)) {
          String monitorSelector = feed.optString(JSONConstants.Fields.SELECTOR);
          assertIsNotEmpty("Endpoint name should not be empty: " + LITE_APP_NAME, monitorSelector);
        }
      }
    }
  }

  private void validateSettings(JSONObject manifestJson) {
    if (!manifestJson.has(JSONConstants.Fields.SETTINGS)) {
      return;
    }

    JSONObject settings = manifestJson.getJSONObject(JSONConstants.Fields.SETTINGS);
    final String LITE_APP_NAME = manifestJson.optString(JSONConstants.Fields.NAME);
    for (String setting : settings.keySet()) {
      assertTrue(String.format("Unexpected setting found: [%s] in [%s]", setting, LITE_APP_NAME),
          JSONConstants.SETTINGS_SET.contains(setting));
    }
  }

  /**
   * Loads a JSON file and can perform multiple validations on it.
   */
  private static class JsonValidatorHelper {
    private final String tag;
    private JSONObject json;

    public JsonValidatorHelper(String tag, File file) {
      if (file == null || !file.exists()) {
        fail("Not found: " + file.getAbsolutePath());
      }

      this.tag = tag;
      try {
        this.json = file.exists() ? new JSONObject(FileUtils.readFully(new FileInputStream(file))) : null;
      } catch (IOException | JSONException e) {
        fail(String.format("Invalid JSON: %s", tag));
      }
    }

    public JsonValidatorHelper assertFieldExists(String field) {
      assertNotNull(String.format("File [%s] is missing the field [%s]", tag, field), json.optString(field, null));
      return this;
    }

    public JSONObject getJSON() {
      return json;
    }
  }
}
