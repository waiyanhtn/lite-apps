package com.chimbori.blocklists;

import com.chimbori.common.FileUtils;

import java.io.File;

public class FilePaths {
  // Inputs
  static final File SRC_ROOT_DIR         = new File(FileUtils.PROJECT_ROOT, "blocklists/");
  static final File SRC_BLOCK_LISTS_JSON = new File(FileUtils.PROJECT_ROOT, "blocklists/index.json");

  // Outputs
  static final File OUT_ROOT_DIR = new File(FileUtils.PROJECT_ROOT, "bin/blocklists/");
}
