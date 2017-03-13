package com.chimbori.liteapps;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BlockListParserTest {
  @Test
  public void testAssembleAllBlockLists() {
    try {
      // BlockListsParser.downloadFromSources();
      assertTrue(BlockListsParser.packageBlockLists(false));
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }
}
