package com.chimbori.schema.manifest;

import com.google.gson.Gson;

import java.io.FileReader;
import java.util.List;

public class Manifest {
  public String manifest_version;
  public String lang;
  public String name;
  public String start_url;
  public String manifest_url;
  public String theme_color;
  public String secondary_color;
  public Settings hermit_settings;
  public List<String> tags;

  public static class Settings {
    public String user_agent;
  }

  public static Manifest fromGson(Gson gson, FileReader fileReader) {
    return gson.fromJson(fileReader, Manifest.class);
  }
}
