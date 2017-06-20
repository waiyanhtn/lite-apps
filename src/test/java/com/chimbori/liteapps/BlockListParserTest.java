package com.chimbori.liteapps;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BlockListParserTest {
  private static final boolean IS_TEST_ENABLED = false;

  @Test
  public void testAssembleAllBlockLists() {
    if (!IS_TEST_ENABLED) {
      return;
    }

    try {
      BlockListsParser.downloadFromSources();
      assertTrue(BlockListsParser.packageBlockLists(false));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }
}
