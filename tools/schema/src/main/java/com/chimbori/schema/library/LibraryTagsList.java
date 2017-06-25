package com.chimbori.schema.library;

import com.google.gson.Gson;

import java.io.Reader;
import java.util.HashSet;
import java.util.List;

public class LibraryTagsList {
  public List<LibraryTag> tags;

  private transient HashSet<String> tagNamesSet;

  private void updateTransientFields() {
    tagNamesSet = new HashSet<>();
    for (LibraryTag tag : tags) {
      tagNamesSet.add(tag.name);
    }
  }

  public void addTag(LibraryTag tag) {
    if (tagNamesSet.contains(tag.name)) {
      return;
    }
    tagNamesSet.add(tag.name);
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
        ", tagNamesSet=" + tagNamesSet +
        '}';
  }
}
