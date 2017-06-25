package com.chimbori.liteapps;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.fail;

/**
 * Generates the library JSON file.
 */
public class LibraryGeneratorTest {
  @Test
  public void testIndexJsonIsWellFormedAndReformat() throws IOException {
    TestHelpers.assertJsonIsWellFormedAndReformat(FilePaths.SRC_TAGS_JSON_FILE);
  }

  @Test
  public void testUpdateTagsJSON() {
    try {
      TagsCollector.updateTagsGson();
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testLibraryDataIsGeneratedSuccessfully() {
    try {
      LibraryGenerator.generateLibraryData();
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }
}
