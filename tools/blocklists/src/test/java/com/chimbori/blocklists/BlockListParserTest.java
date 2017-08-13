package com.chimbori.blocklists;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class BlockListParserTest {
  private static final boolean FETCH_REMOTE_FILES = false;

  @Test
  public void testAssembleAllBlockLists() {
    try {
      if (FETCH_REMOTE_FILES) {
        BlockListsParser.downloadFromSources();
      }
      BlockListsParser.packageBlockLists();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
}
