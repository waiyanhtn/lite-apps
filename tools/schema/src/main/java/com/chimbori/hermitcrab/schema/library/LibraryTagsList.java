package com.chimbori.hermitcrab.schema.library;

import com.google.gson.Gson;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibraryTagsList {
  public List<LibraryTag> tags;

  private transient Map<String, LibraryTag> tagsMap;

  private void updateTransientFields() {
    tagsMap = new HashMap<>();
    for (LibraryTag tag : tags) {
      tagsMap.put(tag.name, tag);
    }
  }

  public void addTag(LibraryTag tag) {
    if (tagsMap.keySet().contains(tag.name)) {
      return;
    }
    tagsMap.put(tag.name, tag);
    tags.add(tag);
  }

  public static LibraryTagsList fromGson(Gson gson, Reader reader) {
    LibraryTagsList libraryTagsList = gson.fromJson(reader, LibraryTagsList.class);
    libraryTagsList.updateTransientFields();
    return libraryTagsList;
  }

  public String toJson(Gson gson) {
    return gson.toJson(this);
  }

  @Override
  public String toString() {
    return "LibraryTagsList{" +
        "tags=" + tags +
        ", tagsMap=" + tagsMap +
        '}';
  }
}
