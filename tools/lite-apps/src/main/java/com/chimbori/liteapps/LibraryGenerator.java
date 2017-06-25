package com.chimbori.liteapps;

import com.chimbori.common.FileUtils;
import com.chimbori.schema.library.LibraryTag;
import com.chimbori.schema.library.LibraryTagsList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.coobird.thumbnailator.Thumbnails;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
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
    LibraryTagsList tagsGson = LibraryTagsList.fromGson(gson, new FileReader(FilePaths.SRC_TAGS_JSON_FILE));

    JSONArray outputLibrary = new JSONArray();
    Map<String, JSONArray> outputTags = new HashMap<>();
    File[] liteAppDirs = FilePaths.SRC_ROOT_DIR.listFiles();
    for (File liteAppDirectory : liteAppDirs) {
      if (!liteAppDirectory.isDirectory()) {
        continue; // Probably a temporary file, like .DS_Store.
      }

      String appName = liteAppDirectory.getName();
      JSONObject outputApp = new JSONObject();
      outputApp.put(JSONConstants.Fields.NAME, appName);

      File manifestJson = new File(liteAppDirectory, FilePaths.MANIFEST_JSON_FILE_NAME);
      if (!manifestJson.exists()) {
        System.err.println("Error: Missing manifest.json for " + liteAppDirectory.getName());
        return false;  // Error, missing manifest.json.
      }

      // Create an entry for this Lite App to be put in the directory index file.
      JSONObject manifest = new JSONObject(FileUtils.readFully(new FileInputStream(manifestJson)));
      outputApp.put(JSONConstants.Fields.URL, manifest.optString(JSONConstants.Fields.START_URL));
      outputApp.put(JSONConstants.Fields.APP, String.format("%s.hermit", appName));
      outputApp.put(JSONConstants.Fields.THEME_COLOR, manifest.optString(JSONConstants.Fields.THEME_COLOR));

      // Set user-agent from the settings stored in the Lite App’s manifest.json.
      JSONObject settings = manifest.optJSONObject(JSONConstants.Fields.SETTINGS);
      if (settings != null &&
          settings.optString(JSONConstants.Fields.USER_AGENT, "").equals(JSONConstants.Values.USER_AGENT_DESKTOP)) {
        outputApp.put(JSONConstants.Fields.USER_AGENT, JSONConstants.Values.USER_AGENT_DESKTOP);
      }

      // Insert this new entry into all the categories that this Lite App belongs to.
      if (!manifest.has(JSONConstants.Fields.TAGS)) {
        System.err.println("JSON tags not found for: " + liteAppDirectory.getName());
        return false;
      }

      JSONArray tags = manifest.getJSONArray(JSONConstants.Fields.TAGS);

      for (int i = 0; i < tags.length(); i++) {
        String tag = tags.getString(i);
        JSONArray tagContent = outputTags.get(tag);
        if (tagContent == null) {
          // If this is the first time we are seeing this tag, create a new JSONArray to hold its contents.
          tagContent = new JSONArray();
          outputTags.put(tag, tagContent);
        }
        tagContent.put(outputApp);

        // Also write tag to the tags.json if not already present.
        tagsGson.addTag(new LibraryTag(tag));
      }

      // Resize the icon to be suitable for the Web, and copy it to the Web-accessible icons directory.
      Thumbnails.of(new File(liteAppDirectory, FilePaths.ICON_FILENAME))
          .outputQuality(1.0f)
          .useOriginalFormat()
          .size(LIBRARY_ICON_SIZE, LIBRARY_ICON_SIZE)
          .imageType(BufferedImage.TYPE_INT_ARGB)
          .toFile(new File(FilePaths.OUT_LIBRARY_ICONS_DIR, appName + FilePaths.ICON_EXTENSION));
    }

    // Write the map of output categories to JSON.
    for (String key : outputTags.keySet()) {
      JSONObject outputCategory = new JSONObject();
      outputCategory.put(JSONConstants.Fields.CATEGORY, key);
      outputCategory.put(JSONConstants.Fields.APPS, outputTags.get(key));
      outputLibrary.put(outputCategory);
    }

    FileUtils.writeFile(FilePaths.OUT_LITE_APPS_JSON, outputLibrary.toString(2));

    // Write out tags.json if we ended up adding any tags to it.
    FileUtils.writeFile(FilePaths.SRC_TAGS_JSON_FILE, tagsGson.toJson(gson));

    return true;
  }
}
