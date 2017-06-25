package com.chimbori.liteapps;

import com.chimbori.common.FileUtils;
import com.chimbori.schema.library.LibraryApp;
import com.chimbori.schema.library.Library;
import com.chimbori.schema.library.LibraryTag;
import com.chimbori.schema.library.LibraryTagsList;
import com.chimbori.schema.manifest.Manifest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.coobird.thumbnailator.Thumbnails;

import org.json.JSONException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class LibraryGenerator {
  private static final int LIBRARY_ICON_SIZE = 112;

  /**
   * Individual manifest.json files do not contain any information about the organization of the
   * Lite Apps in the Library (e.g. categories, order within category, whether it should be
   * displayed or not. This metadata is stored in a separate index.json file. To minimize
   * duplication & to preserve a single source of truth, this file does not contain actual URLs
   * or anything about a Lite App other than its name (same as the directory name).
   * <p>
   * This generator tool combines the basic organizational metadata from index.json & detailed
   * Lite Apps data from * / manifest.json files. It outputs bin/lite-apps.json,
   * which is used as the basis for generating the Hermit Library page at
   * https://hermit.chimbori.com/library.
   */
  public static boolean generateLibraryData() throws IOException, JSONException {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Read the list of all known tags from the tags.json file. In case we discover any new tags,
    // we will add them to this file, taking care not to overwrite those that already exist.
    LibraryTagsList globalTags = LibraryTagsList.fromGson(gson, new FileReader(FilePaths.SRC_TAGS_JSON_FILE));
    Library outputLibrary = new Library(globalTags);

    File[] liteAppDirs = FilePaths.SRC_ROOT_DIR.listFiles();
    for (File liteAppDirectory : liteAppDirs) {
      if (!liteAppDirectory.isDirectory()) {
        continue; // Probably a temporary file, like .DS_Store.
      }

      String appName = liteAppDirectory.getName();
      File manifestJsonFile = new File(liteAppDirectory, FilePaths.MANIFEST_JSON_FILE_NAME);
      if (!manifestJsonFile.exists()) {
        throw new ManifestMissingException(appName);
      }

      // Create an entry for this Lite App to be put in the directory index file.
      Manifest manifest = Manifest.fromGson(gson, new FileReader(manifestJsonFile));

      LibraryApp outputApp = new LibraryApp();
      outputApp.url = manifest.start_url;
      outputApp.name = appName;
      outputApp.app = String.format("%s.hermit", appName);
      outputApp.theme_color = manifest.theme_color;

      // Set user-agent from the settings stored in the Lite App’s manifest.json.
      String userAgent = manifest.hermit_settings != null ? manifest.hermit_settings.user_agent : null;
      if (JSONConstants.Values.USER_AGENT_DESKTOP.equals(userAgent)) {
        outputApp.user_agent = JSONConstants.Values.USER_AGENT_DESKTOP;
      }

      outputLibrary.addAppToCategories(outputApp, manifest.tags);

      // Resize the icon to be suitable for the Web, and copy it to the Web-accessible icons directory.
      Thumbnails.of(new File(liteAppDirectory, FilePaths.ICON_FILENAME))
          .outputQuality(1.0f)
          .useOriginalFormat()
          .size(LIBRARY_ICON_SIZE, LIBRARY_ICON_SIZE)
          .imageType(BufferedImage.TYPE_INT_ARGB)
          .toFile(new File(FilePaths.OUT_LIBRARY_ICONS_DIR, appName + FilePaths.ICON_EXTENSION));
    }

    FileUtils.writeFile(FilePaths.OUT_LIBRARY_JSON, outputLibrary.toJson(gson));
    return true;
  }

  public static boolean updateTagsGson() throws IOException, JSONException {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Read the list of all known tags from the tags.json file. In case we discover any new tags,
    // we will add them to this file, taking care not to overwrite those that already exist.
    LibraryTagsList tagsGson = LibraryTagsList.fromGson(gson, new FileReader(FilePaths.SRC_TAGS_JSON_FILE));

    Map<String, LibraryTag> globalTags = new HashMap<>();
    File[] liteAppDirs = FilePaths.SRC_ROOT_DIR.listFiles();
    for (File liteAppDirectory : liteAppDirs) {
      if (!liteAppDirectory.isDirectory()) {
        continue; // Probably a temporary file, like .DS_Store.
      }

      File manifestJsonFile = new File(liteAppDirectory, FilePaths.MANIFEST_JSON_FILE_NAME);
      if (!manifestJsonFile.exists()) {
        throw new ManifestMissingException(liteAppDirectory.getName());
      }

      Manifest manifest = Manifest.fromGson(gson, new FileReader(manifestJsonFile));

      // For all tags applied to this manifest, check if they exist in the global tags list.
      for (String tagName : manifest.tags) {
        LibraryTag tag = globalTags.get(tagName);
        if (tag == null) {
          // If this is the first time we are seeing this tag, create a new JSONArray to hold its contents.
          LibraryTag newTag = new LibraryTag(tagName);
          globalTags.put(tagName, newTag);
          tagsGson.addTag(newTag);
        }
      }
    }

    // Write the tags to JSON
    FileUtils.writeFile(FilePaths.SRC_TAGS_JSON_FILE, tagsGson.toJson(gson));

    return true;
  }

  private static class ManifestMissingException extends RuntimeException {
    public ManifestMissingException(String liteAppName) {
      super("Error: Missing manifest.json for " + liteAppName);
    }
  }
}
