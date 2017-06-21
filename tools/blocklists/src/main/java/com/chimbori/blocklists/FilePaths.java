package com.chimbori.blocklists;

import java.io.File;

public class FilePaths {
  private static final String INDEX_JSON_FILE_NAME = "index.json";

  public static final File SRC_ROOT_DIR = new File("../../blocklists/");
  public static final File OUT_ROOT_DIR = new File("../../bin/blocklists");

  public static final File BLOCK_LISTS_INDEX_JSON = new File(FilePaths.SRC_ROOT_DIR, INDEX_JSON_FILE_NAME);
}
