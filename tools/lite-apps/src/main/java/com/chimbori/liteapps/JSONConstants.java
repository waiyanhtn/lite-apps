package com.chimbori.liteapps;

import java.util.Arrays;
import java.util.HashSet;

class JSONConstants {
  public class Values {
    public static final String EN = "en";
    public static final String PLAY = "play";
    public static final String USER_AGENT_DESKTOP = "desktop";
  }

  public static final HashSet<String> SETTINGS_SET = new HashSet<>(Arrays.asList(
      "block_malware",
      "block_popups",
      "block_third_party_cookies",
      "browser",
      "day_night_mode",
      "do_not_track",
      "in_app",
      "javascript",
      "load_images",
      "night_mode_page_style",
      "open_links",
      "preferred_view",
      "pull_to_refresh",
      "save_data",
      "scroll_to_top",
      "text_zoom",
      "user_agent"
  ));
}
