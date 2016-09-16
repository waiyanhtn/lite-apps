package com.chimbori.liteapps;

import org.junit.Test;

import java.io.File;

/**
 * Ensures that all Lite Apps can be packaged without error.
 */
public class ManifestPackagerTest {
  @Test
  public void testAllManifestsPackagedSuccessfully() {
    ManifestPackager.packageAllManifests(new File("lite-apps/"));
  }
}
