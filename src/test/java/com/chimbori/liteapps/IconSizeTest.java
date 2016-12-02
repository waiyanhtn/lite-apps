package com.chimbori.liteapps;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;

/**
 * Ensures all icons are exactly 300x300 in size.
 */
@RunWith(Parameterized.class)
public class IconSizeTest extends ParameterizedLiteAppTest {
  public IconSizeTest(File liteApp) {
    super(liteApp);
  }

  @Test
  public void testIconIs300x300() {
    TestHelpers.assertThatIconIs300x300(new File(liteApp, FileUtils.ICON_FILENAME));
  }
}
