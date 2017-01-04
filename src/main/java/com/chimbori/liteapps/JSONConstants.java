package com.chimbori.liteapps;

class JSONConstants {

  public class Fields {
    // Used in index.json & lite-apps.json.
    public static final String CATEGORY = "category";
    public static final String APPS = "apps";
    public static final String APP = "app";

    // Used in manifest.json.
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String START_URL = "start_url";
    public static final String LANG = "lang";
    public static final String MANIFEST_URL = "manifest_url";
    public static final String THEME_COLOR = "theme_color";
    public static final String SECONDARY_COLOR = "secondary_color";
    public static final String MANIFEST_VERSION = "manifest_version";
    public static final String ICONS = "icons";
    public static final String RELATED_APPLICATIONS = "related_applications";
    public static final String PLATFORM = "platform";
    public static final String ID = "id";
    public static final String SRC = "src";
    public static final String SETTINGS = "hermit_settings";
    public static final String USER_AGENT = "user_agent";
  }

  public class Values {
    public static final String EN = "en";
    public static final String PLAY = "play";
    public static final String USER_AGENT_DESKTOP = "desktop";
  }

  public class Roles {
    public static final String FEEDS = "hermit_feeds";
    public static final String BOOKMARKS = "hermit_bookmarks";
    public static final String CREATE = "hermit_create";
    public static final String SHARE = "hermit_share";
    public static final String SEARCH = "hermit_search";
  }
}
