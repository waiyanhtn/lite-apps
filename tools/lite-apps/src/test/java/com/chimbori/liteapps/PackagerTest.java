package com.chimbori.liteapps;

import com.chimbori.common.FileUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Ensures that all Lite Apps can be packaged without error.
 */
@RunWith(Parameterized.class)
public class PackagerTest extends ParameterizedLiteAppTest {
  public PackagerTest(File liteApp) {
    super(liteApp);
  }

  @Before
  public void setUp() {
    FilePaths.OUT_ROOT_DIR.delete();
  }

  @Test
  public void testParameterizedChecker() {
    assertTrue("Packaging failed for " + liteApp.getName(), Packager.packageManifest(liteApp));
  }
}
