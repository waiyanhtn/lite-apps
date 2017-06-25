package com.chimbori.schema.library;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Library {
  public ArrayList<LiteAppCategoryWithApps> categories;

  public Library(LibraryTagsList globalTags) {
    categories = new ArrayList<>();
    for (LibraryTag tag : globalTags.tags) {
      categories.add(new LiteAppCategoryWithApps(tag));
    }
  }

  public static class LiteAppCategoryWithApps {
    public LibraryTag category;
    public ArrayList<LibraryApp> apps;

    public LiteAppCategoryWithApps(LibraryTag category) {
      this.category = category;
      apps = new ArrayList<>();
    }
  }

  public void addAppToCategories(LibraryApp app, List<String> categories) {
    for (LiteAppCategoryWithApps categoryWithApps : this.categories) {
       for (String categoryName : categories) {
         if (categoryWithApps.category.name.equals(categoryName)) {
           categoryWithApps.apps.add(app);
         }
       }
    }
  }

  public String toJson(Gson gson) {
    return gson.toJson(this);
  }
}
