package com.chimbori.schema.manifest;

import com.google.gson.Gson;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Manifest {
  public int manifest_version;
  public String lang;
  public String name;
  public String start_url;
  public String manifest_url;
  public String theme_color;
  public String secondary_color;
  public Settings hermit_settings;

  public List<String> tags;
  public List<Icon> icons;
  public Collection<Endpoint> hermit_bookmarks;
  public Collection<Endpoint> hermit_feeds;
  public Collection<Endpoint> hermit_create;
  public Collection<Endpoint> hermit_share;
  public Collection<Endpoint> hermit_search;
  public Collection<Endpoint> hermit_monitors;

  public Collection<RelatedApplication> related_applications;

  public Manifest() {
    tags = new ArrayList<>();
    icons = new ArrayList<>();
    hermit_bookmarks = new ArrayList<>();
    hermit_feeds = new ArrayList<>();
    related_applications = new ArrayList<>();
  }

  public void addIcon(Icon icon) {
    if (!icons.contains(icon)) {
      icons.add(icon);
    }
  }

  public static Manifest fromGson(Gson gson, FileReader fileReader) {
    return gson.fromJson(fileReader, Manifest.class);
  }

  public String toJson(Gson gson) {
    // Prevent Gson from writing out fields if they contain no references.
    // TODO: Consider converting this to a TypeAdapter.
    if (tags.size() == 0) {
      tags = null;
    }
    if (icons.size() == 0) {
      icons = null;
    }
    if (hermit_bookmarks.size() == 0) {
      hermit_bookmarks = null;
    }
    if (hermit_feeds.size() == 0) {
      hermit_feeds = null;
    }
    if (related_applications.size() == 0) {
      related_applications = null;
    }
    return gson.toJson(this);
  }
}
