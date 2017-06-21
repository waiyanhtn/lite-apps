package com.chimbori.liteapps;

import java.io.File;

public class FilePaths {
  public static final String ICON_EXTENSION = ".png";

  // Filenames
  public static final String MANIFEST_JSON_FILE_NAME = "manifest.json";
  public static final String LOCALES_DIR_NAME = "_locales";
  public static final String MESSAGES_JSON_FILE_NAME = "messages.json";
  private static final String INDEX_JSON_FILE_NAME = "index.json";
  private static final String LITE_APPS_JSON_FILE_NAME = "lite-apps.json";
  public static final String ICON_FILENAME = "icon.png";

  // Directories
  public static final File SRC_ROOT_DIR = new File("../../lite-apps/");
  public static final File OUT_ROOT_DIR = new File("../../bin/");
  public static final File OUT_LITE_APPS_DIR = new File(OUT_ROOT_DIR, "lite-apps/");
  public static final File OUT_LIBRARY_ICONS_DIR = new File(OUT_ROOT_DIR, "library/112x112/");

  // Files
  public static final File SRC_INDEX_JSON = new File(SRC_ROOT_DIR, INDEX_JSON_FILE_NAME);
  public static final File OUT_LITE_APPS_JSON = new File(OUT_LITE_APPS_DIR, LITE_APPS_JSON_FILE_NAME);

  static {
    OUT_ROOT_DIR.mkdirs();
    OUT_LITE_APPS_DIR.mkdirs();
    OUT_LIBRARY_ICONS_DIR.mkdirs();
  }
}