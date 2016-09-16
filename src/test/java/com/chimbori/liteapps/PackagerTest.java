package com.chimbori.liteapps;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Ensures that all Lite Apps can be packaged without error.
 */
public class PackagerTest {
  @Before
  public void setUp() {
    FileUtils.OUT_ROOT_DIR.delete();
  }

  @Test
  public void testAllManifestsPackagedSuccessfully() {
    Packager.packageAllManifests(FileUtils.SRC_ROOT_DIR);
  }

  @Test
  public void testLibraryDataIsGeneratedSuccessfully() {
    try {
      assertTrue(Packager.generateLibraryData());
    } catch (IOException | JSONException e) {
      fail(e.getMessage());
    }
  }
}
