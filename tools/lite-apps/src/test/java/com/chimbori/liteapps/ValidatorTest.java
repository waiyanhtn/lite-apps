package com.chimbori.liteapps;

import com.chimbori.schema.manifest.Endpoint;
import com.chimbori.schema.manifest.Manifest;
import com.chimbori.schema.manifest.RelatedApplication;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
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
    FilePaths.OUT_LITE_APPS_DIR.delete();
  }

  @Test
  public void testManifestIsValid() {
    File manifestFile = new File(liteApp, FilePaths.MANIFEST_JSON_FILE_NAME);
    Manifest manifest = readManifest(manifestFile);
    String tag = liteApp.getName();

    assertFieldExists(tag, "name", manifest.name);
    assertFieldExists(tag, "start_url", manifest.start_url);
    assertFieldExists(tag, "lang", manifest.lang);
    assertFieldExists(tag, "manifest_url", manifest.manifest_url);
    assertFieldExists(tag, "theme_color", manifest.theme_color);
    assertFieldExists(tag, "secondary_color", manifest.secondary_color);
    assertFieldExists(tag, "manifest_version", manifest.manifest_version);
    assertFieldExists(tag, "icons", manifest.icons);

    // Test that the "manifest_url" field contains a valid URL.
    try {
      URL manifestUrl = new URL(manifest.manifest_url);
      assertEquals("https", manifestUrl.getProtocol());
      assertEquals("hermit.chimbori.com", manifestUrl.getHost());
      assertTrue(manifestUrl.getPath().startsWith("/lite-apps/"));
      assertTrue(manifestUrl.getPath().endsWith(".hermit"));
      assertEquals(liteApp.getName() + ".hermit", new File(URLDecoder.decode(manifestUrl.getFile())).getName());
    } catch (MalformedURLException e) {
      fail(e.getMessage());
    }

    // Test that colors are valid hex colors.
    assertTrue(String.format("[%s] theme_color should be a valid hex color", tag),
        HEX_COLOR_PATTERN.matcher(manifest.theme_color).matches());
    assertTrue(String.format("[%s] secondary_color should be a valid hex color", tag),
        HEX_COLOR_PATTERN.matcher(manifest.secondary_color).matches());

    // Test that the name of the icon file is "icon.png" & that the file exists.
    // Although any filename should work, having it be consistent in the library can let us
    // avoid a filename lookup in automated tests and refactors.
    assertEquals(FilePaths.ICON_FILENAME, manifest.icons.get(0).src);
    assertTrue(new File(liteApp, FilePaths.ICON_FILENAME).exists());

    // Test Endpoints for basic parseability.
    validateEndpoints(tag, manifest.hermit_bookmarks, EndpointRole.BOOKMARK);
    validateEndpoints(tag, manifest.hermit_feeds, EndpointRole.FEED);
    validateEndpoints(tag, manifest.hermit_create, EndpointRole.CREATE);
    validateEndpoints(tag, manifest.hermit_share, EndpointRole.SHARE);
    validateEndpoints(tag, manifest.hermit_search, EndpointRole.SEARCH);
    validateEndpoints(tag, manifest.hermit_monitors, EndpointRole.MONITOR);

    // Test all Settings to see whether they belong to our whitelisted set of allowable strings.
    validateSettings(tag, manifestFile);

    // Test "related_apps" for basic sanity, that if one exists, then it’s pointing to a Play Store app.
    for (RelatedApplication relatedApplication : manifest.related_applications) {
      assertEquals(JSONConstants.Values.PLAY, relatedApplication.platform);
      assertFalse(relatedApplication.id.isEmpty());
      assertTrue(relatedApplication.url.startsWith("https://play.google.com/store/apps/details?id="));
      assertTrue(relatedApplication.url.endsWith(relatedApplication.id));
    }

    // Test that if any localization files are present, then they are well-formed.
    File localesDirectory = new File(liteApp, FilePaths.LOCALES_DIR_NAME);
    if (localesDirectory.exists()) {
      File[] localizations = localesDirectory.listFiles(File::isDirectory);
      for (File localization : localizations) {
        File messagesFile = new File(localization, FilePaths.MESSAGES_JSON_FILE_NAME);
        // With no specific field checks, we at least validate that the file is well-formed JSON.
        try {
          TestHelpers.assertJsonIsWellFormedAndReformat(messagesFile);
        } catch (IOException e) {
          fail(e.getMessage());
        }
      }
    }
  }

  private void validateEndpoints(String tag, Collection<Endpoint> endpoints, EndpointRole role) {
    if (endpoints != null) {
      for (Endpoint endpoint : endpoints) {
        assertIsNotEmpty("Endpoint name should not be empty: " + tag, endpoint.name);
        assertIsURL("Endpoint should have a valid URL: " + tag, endpoint.url);

        if (role == EndpointRole.SEARCH) {
          assertTrue(endpoint.url, endpoint.url.contains("%s"));

        } else if (role == EndpointRole.SHARE) {
          assertTrue(endpoint.url, endpoint.url.contains("%s")
              || endpoint.url.contains("%t")
              || endpoint.url.contains("%u"));

        } else if (role == EndpointRole.MONITOR) {
          assertIsNotEmpty("Endpoint name should not be empty: " + tag, endpoint.selector);
        }
      }
    }
  }

  private void validateSettings(String tag, File manifest) {
    Gson gson = new Gson();
    LinkedTreeMap<String, Object> json = null;
    try {
      json = (LinkedTreeMap<String, Object>) gson.fromJson(new FileReader(manifest), Object.class);
    } catch (FileNotFoundException e) {
      fail(e.getMessage());
    }

    LinkedTreeMap settings = (LinkedTreeMap<String, Object>) json.get("hermit_settings");
    if (settings != null) {
      for (Object key : settings.keySet()) {
        assertTrue(String.format("Unexpected setting found: [%s] in [%s]", key, tag),
          JSONConstants.SETTINGS_SET.contains(key));
      }
    }
  }

  public Manifest readManifest(File file) {
    if (file == null || !file.exists()) {
      fail("Not found: " + file.getAbsolutePath());
    }

    Gson gson = new Gson();
    try {
      return file.exists() ? gson.fromJson(new FileReader(file), Manifest.class) : null;
    } catch (IOException e) {
      fail(String.format("Invalid JSON: %s", file.getName()));
      return null;
    }
  }

  public static void assertFieldExists(String tag, String field, Object value) {
    assertNotNull(String.format("File [%s] is missing the field [%s]", tag, field), value);
  }
}
