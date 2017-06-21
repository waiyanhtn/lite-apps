package com.chimbori.liteapps;

import java.util.Arrays;
import java.util.HashSet;

class JSONConstants {

  public class Fields {
    // Used in index.json & lite-apps.json.
    public static final String CATEGORY = "category";
    public static final String APPS = "apps";
    public static final String APP = "app";

    // Used in manifest.json.
    public static final String DISPLAY = "display";
    public static final String ICONS = "icons";
    public static final String ID = "id";
    public static final String LANG = "lang";
    public static final String MANIFEST_URL = "manifest_url";
    public static final String MANIFEST_VERSION = "manifest_version";
    public static final String NAME = "name";
    public static final String PLATFORM = "platform";
    public static final String RELATED_APPLICATIONS = "related_applications";
    public static final String SECONDARY_COLOR = "secondary_color";
    public static final String SELECTOR = "selector";
    public static final String SETTINGS = "hermit_settings";
    public static final String SRC = "src";
    public static final String START_URL = "start_url";
    public static final String THEME_COLOR = "theme_color";
    public static final String URL = "url";
    public static final String USER_AGENT = "user_agent";
  }

  public class Values {
    public static final String EN = "en";
    public static final String PLAY = "play";
    public static final String USER_AGENT_DESKTOP = "desktop";
  }

  public class Roles {
    public static final String BOOKMARKS = "hermit_bookmarks";
    public static final String CREATE = "hermit_create";
    public static final String FEEDS = "hermit_feeds";
    public static final String MONITORS = "hermit_monitors";
    public static final String SEARCH = "hermit_search";
    public static final String SHARE = "hermit_share";
  }

  public class Settings {
    public static final String BLOCK_MALWARE = "block_malware";
    public static final String BLOCK_POPUPS = "block_popups";
    public static final String BLOCK_THIRD_PARTY_COOKIES = "block_third_party_cookies";
    public static final String DO_NOT_TRACK = "do_not_track";
    public static final String LOAD_IMAGES = "load_images";
    public static final String OPEN_LINKS = "open_links";
    public static final String OPEN_LINKS_IN_APP = "in_app";
    public static final String OPEN_LINKS_IN_BROWSER = "browser";
    public static final String PREFERRED_VIEW = "preferred_view";
    public static final String DAY_NIGHT_MODE = "day_night_mode";
    public static final String NIGHT_MODE_PAGE_STYLE = "night_mode_page_style";
    public static final String SAVE_DATA = "save_data";
    public static final String SCROLL_TO_TOP = "scroll_to_top";
    public static final String PULL_TO_REFRESH = "pull_to_refresh";
    public static final String TEXT_ZOOM = "text_zoom";
    public static final String USER_AGENT = "user_agent";
    public static final String JAVASCRIPT_ENABLED = "javascript";
  }

  public static final HashSet<String> SETTINGS_SET = new HashSet<>(Arrays.asList(
      JSONConstants.Settings.BLOCK_MALWARE,
      JSONConstants.Settings.BLOCK_POPUPS,
      JSONConstants.Settings.BLOCK_THIRD_PARTY_COOKIES,
      JSONConstants.Settings.DO_NOT_TRACK,
      JSONConstants.Settings.LOAD_IMAGES,
      JSONConstants.Settings.OPEN_LINKS,
      JSONConstants.Settings.OPEN_LINKS_IN_APP,
      JSONConstants.Settings.OPEN_LINKS_IN_BROWSER,
      JSONConstants.Settings.PREFERRED_VIEW,
      JSONConstants.Settings.DAY_NIGHT_MODE,
      JSONConstants.Settings.NIGHT_MODE_PAGE_STYLE,
      JSONConstants.Settings.SAVE_DATA,
      JSONConstants.Settings.SCROLL_TO_TOP,
      JSONConstants.Settings.PULL_TO_REFRESH,
      JSONConstants.Settings.TEXT_ZOOM,
      JSONConstants.Settings.USER_AGENT,
      JSONConstants.Settings.JAVASCRIPT_ENABLED
  ));
}
