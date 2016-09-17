package com.chimbori.liteapps;

public class JSONConstants {

  public class Fields {
    // Used in lite-apps.json.
    public static final String CATEGORY = "category";
    public static final String APPS = "apps";
    public static final String URL = "url";
    public static final String APP = "app";

    // Used in manifest.json.
    public static final String NAME = "name";
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

    /**
     * A special designated field that is automatically inserted when a manifest is generated via
     * scaffolding. Once the manifest has been vetted by hand, this field should be removed.
     * When this field is absent, the Lite App is considered ready for release, and scaffolding
     * tools will avoid modifying the manifest after that point.
     */
    public static final String UNDER_DEVELOPMENT = "UNDER_DEVELOPMENT";
  }

  public class Values {
    public static final String EN = "en";
    public static final String PLAY = "play";
  }
}
