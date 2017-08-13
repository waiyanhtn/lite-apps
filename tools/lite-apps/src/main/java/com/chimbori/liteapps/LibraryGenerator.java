package com.chimbori.liteapps;

import com.chimbori.common.FileUtils;
import com.chimbori.hermitcrab.schema.common.GsonInstance;
import com.chimbori.hermitcrab.schema.library.Library;
import com.chimbori.hermitcrab.schema.library.LibraryApp;
import com.chimbori.hermitcrab.schema.library.LibraryTagsList;
import com.chimbori.hermitcrab.schema.manifest.Manifest;
import com.google.gson.Gson;

import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

class LibraryGenerator {
  private static final int LIBRARY_ICON_SIZE = 112;

  private static final String USER_AGENT_DESKTOP = "desktop";

  /**
   * Individual manifest.json files do not contain any information about the organization of the
   * Lite Apps in the Library (e.g. categories, order within category, whether it should be
   * displayed or not. This metadata is stored in a separate index.json file. To minimize
   * duplication & to preserve a single source of truth, this file does not contain actual URLs
   * or anything about a Lite App other than its name (same as the directory name).
   * <p>
   * This generator tool combines the basic organizational metadata from index.json & detailed
   * Lite Apps data from * / manifest.json files. It outputs library.json,
   * which is used as the basis for generating the Hermit Library page at
   * https://hermit.chimbori.com/library.
   */
  public static void generateLibraryData() throws IOException {
    Gson gson = GsonInstance.getPrettyPrinter();

    // Read the list of all known tags from the tags.json file. In case we discover any new tags,
    // we will add them to this file, taking care not to overwrite those that already exist.
    LibraryTagsList globalTags = LibraryTagsList.fromGson(gson, new FileReader(FilePaths.SRC_TAGS_JSON_FILE));
    Library outputLibrary = new Library(globalTags);

    File[] liteAppDirs = FilePaths.SRC_ROOT_DIR.listFiles();
    for (File liteAppDirectory : liteAppDirs) {
      if (!liteAppDirectory.isDirectory()) {
        continue; // Probably a temporary file, like .DS_Store.
      }

      File iconsDirectory = new File(liteAppDirectory, FilePaths.ICONS_DIR_NAME);

      String appName = liteAppDirectory.getName();
      File manifestJsonFile = new File(liteAppDirectory, FilePaths.MANIFEST_JSON_FILE_NAME);
      if (!manifestJsonFile.exists()) {
        throw new MissingManifestException(appName);
      }

      // Create an entry for this Lite App to be put in the directory index file.
      Manifest manifest = gson.fromJson(new FileReader(manifestJsonFile), Manifest.class);

      LibraryApp outputApp = new LibraryApp();
      outputApp.url = manifest.startUrl;
      outputApp.name = appName;
      outputApp.theme_color = manifest.themeColor;
      outputApp.priority = manifest.priority;

      // Set user-agent from the settings stored in the Lite App’s manifest.json.
      String userAgent = manifest.hermitSettings != null ? manifest.hermitSettings.userAgent : null;
      if (USER_AGENT_DESKTOP.equals(userAgent)) {
        outputApp.user_agent = USER_AGENT_DESKTOP;
      }

      outputLibrary.addAppToCategories(outputApp, manifest.tags);

      // Resize the icon to be suitable for the Web, and copy it to the Web-accessible icons directory.
      File thumbnailImage = new File(FilePaths.OUT_LIBRARY_ICONS_DIR, appName + FilePaths.ICON_EXTENSION);
      if (!thumbnailImage.exists()) {
        Thumbnails.of(new File(iconsDirectory, FilePaths.FAVICON_FILENAME))
            .outputQuality(1.0f)
            .useOriginalFormat()
            .size(LIBRARY_ICON_SIZE, LIBRARY_ICON_SIZE)
            .imageType(BufferedImage.TYPE_INT_ARGB)
            .toFile(thumbnailImage);
      }
    }

    FileUtils.writeFile(FilePaths.OUT_LIBRARY_JSON, outputLibrary.toJson(gson));
  }
}
