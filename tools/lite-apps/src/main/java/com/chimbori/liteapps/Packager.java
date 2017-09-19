package com.chimbori.liteapps;

import com.chimbori.FilePaths;
import com.chimbori.common.FileUtils;
import com.eclipsesource.json.Json;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Packages all Lite Apps into their corresponding .hermit packages.
 * Does not validate each Lite App prior to packaging: it is assumed that this is run on a green
 * build which has already passed all validation tests.
 */
class Packager {
  /**
   * Packages a single manifest from a source directory & individual files into a zipped file and
   * places it in the correct location.
   */
  public static boolean packageManifest(File liteAppDirectory) {
    File manifestJsonFile = new File(liteAppDirectory, FilePaths.MANIFEST_JSON_FILE_NAME);
    File liteAppZippedFile = new File(FilePaths.OUT_LITE_APPS_DIR, String.format("%s.hermit", liteAppDirectory.getName()));
    try {
      Json.parse(new FileReader(manifestJsonFile));
    } catch (IOException e) {
      return false;
    }

    FileUtils.zip(liteAppDirectory, liteAppZippedFile);
    return true;
  }
}
