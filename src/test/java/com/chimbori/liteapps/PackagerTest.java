package com.chimbori.liteapps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

/**
 * Ensures that all Lite Apps can be packaged without error.
 */
@RunWith(Parameterized.class)
public class PackagerTest {
  private final File liteApp;

  @Before
  public void setUp() {
    FileUtils.OUT_ROOT_DIR.delete();
  }

  public PackagerTest(File liteApp) {
    this.liteApp = liteApp;
  }

  @Parameterized.Parameters
  public static Collection listOfLiteApps() {
    return Arrays.asList(FileUtils.SRC_ROOT_DIR.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    }));
  }

  @Test
  public void testParameterizedChecker() {
    assertTrue(Packager.packageManifest(liteApp));
  }
}
