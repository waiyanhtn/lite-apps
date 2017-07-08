package com.chimbori.liteapps;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Generates the library JSON file, updates tags.json, and reformat it.
 */
public class LibraryGeneratorTest {
  @Test
  public void testTagsJSONIsWellFormedAndReformat() throws IOException {
    TestHelpers.assertJsonIsWellFormedAndReformat(FilePaths.SRC_TAGS_JSON_FILE);
  }

  @Test
  public void testUpdateTagsJSON() {
    try {
      TagsCollector.updateTagsJson();
    } catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testLibraryDataIsGeneratedSuccessfully() throws IOException {
    LibraryGenerator.generateLibraryData();
    assertTrue(true);
  }
}
