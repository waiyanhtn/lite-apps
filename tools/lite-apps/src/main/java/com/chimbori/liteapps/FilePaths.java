package com.chimbori.liteapps;

import com.chimbori.common.FileUtils;

import java.io.File;

public class FilePaths {
  // Filenames
  static final String MANIFEST_JSON_FILE_NAME = "manifest.json";
  static final String MESSAGES_JSON_FILE_NAME = "messages.json";
  static final String LOCALES_DIR_NAME = "_locales";
  static final String ICON_FILENAME = "icon.png";
  static final String ICON_EXTENSION = ".png";

  // Inputs
  static final File SRC_ROOT_DIR          = new File(FileUtils.PROJECT_ROOT, "lite-apps/");
  static final File SRC_TAGS_JSON_FILE    = new File(FileUtils.PROJECT_ROOT, "lite-apps/tags.json");

  // Outputs
  static final File OUT_LITE_APPS_DIR     = new File(FileUtils.PROJECT_ROOT, "bin/lite-apps/");
  static final File OUT_LIBRARY_ICONS_DIR = new File(FileUtils.PROJECT_ROOT, "bin/library/112x112/");
  static final File OUT_LIBRARY_JSON      = new File(FileUtils.PROJECT_ROOT, "bin/library/library.json");

  static {
    OUT_LITE_APPS_DIR.mkdirs();
    OUT_LIBRARY_ICONS_DIR.mkdirs();
  }
}