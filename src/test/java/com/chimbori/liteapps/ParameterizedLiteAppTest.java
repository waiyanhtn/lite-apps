package com.chimbori.liteapps;

import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * Base class for all parameterized tests that act on a single Lite App. The input is a {@code File}
 * that’s the root directory of the Lite App.
 */
public class ParameterizedLiteAppTest {
  protected final File liteApp;

  public ParameterizedLiteAppTest(File liteApp) {
    this.liteApp = liteApp;
  }

  @Parameterized.Parameters
  public static Collection listOfLiteApps() {
    return Arrays.asList(FileUtils.SRC_ROOT_DIR.listFiles(File::isDirectory));
  }
}
