package com.chimbori.blocklists;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class BlockListParserTest {
  private static final boolean IS_TEST_ENABLED = true;

  @Test
  public void testAssembleAllBlockLists() {
    if (!IS_TEST_ENABLED) {
      return;
    }

    try {
      BlockListsParser.downloadFromSources();
      Assert.assertTrue(BlockListsParser.packageBlockLists(false));
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
}
