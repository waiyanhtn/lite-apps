package com.chimbori.liteapps;

import com.chimbori.common.FileUtils;
import com.chimbori.schema.library.LibraryTag;
import com.chimbori.schema.library.LibraryTagsList;
import com.chimbori.schema.manifest.Manifest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TagsCollector {
  public static void updateTagsGson() throws IOException {
    Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();

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
        throw new MissingManifestException(liteAppDirectory.getName());
      }

      Manifest manifest = Manifest.fromGson(gson, new FileReader(manifestJsonFile));

      // For all tags applied to this manifest, check if they exist in the global tags list.
      if (manifest.tags != null) {
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
    }

    // Write the tags to JSON
    FileUtils.writeFile(FilePaths.SRC_TAGS_JSON_FILE, tagsGson.toJson(gson));
  }
}
