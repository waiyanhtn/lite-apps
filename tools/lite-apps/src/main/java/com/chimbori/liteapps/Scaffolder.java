package com.chimbori.liteapps;

import com.chimbori.common.ColorExtractor;
import com.chimbori.common.FileUtils;
import com.chimbori.common.Log;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * Generates a skeleton Lite App with as many fields pre-filled as possible.
 */
class Scaffolder {
  private static final String MANIFEST_URL_TEMPLATE = "https://hermit.chimbori.com/lite-apps/%s.hermit";
  private static final String PLAY_STORE_URL_TEMPLATE = "https://play.google.com/store/apps/details?id=";
  private static final String OPTION_URL = "url";
  private static final String OPTION_TITLE = "title";

  /**
   * The lite-apps.json file (containing metadata about all Lite Apps) is used as the basis for
   * generating scaffolding for the Lite App manifests.
   */
  private static boolean createScaffolding(String startUrl, String appName) throws JSONException, IOException {
    File liteAppDirectoryRoot = new File(FilePaths.SRC_ROOT_DIR, appName);

    JSONObject root;
    File manifestJson = new File(liteAppDirectoryRoot, FilePaths.MANIFEST_JSON_FILE_NAME);
    // If the manifest.json exists, read it before modifying, else create a new JSON object.
    if (manifestJson.exists()) {
      root = new JSONObject(FileUtils.readFully(new FileInputStream(manifestJson)));
    } else {
      Log.i("Creating new Lite App %s", appName);
      // Create the root directory if it doesn’t exist yet.
      liteAppDirectoryRoot.mkdirs();
      root = new JSONObject();

      // Constant fields, same for all apps.
      root.put(JSONConstants.Fields.MANIFEST_VERSION, 1);
      root.put(JSONConstants.Fields.LANG, JSONConstants.Values.EN);

      // Fields that can be populated from the data provided on the command-line.
      root.put(JSONConstants.Fields.NAME, appName);
      root.put(JSONConstants.Fields.START_URL, startUrl);
      root.put(JSONConstants.Fields.MANIFEST_URL, String.format(MANIFEST_URL_TEMPLATE,
          URLEncoder.encode(appName, "UTF-8").replace("+", "%20")));

      // Empty fields that must be manually populated.
      root.put(JSONConstants.Fields.TAGS, Arrays.asList(new String[]{"TODO"} ));
    }

    // Scrape the Web looking for RSS & Atom feeds, theme colors, and site metadata.
    Log.i("Fetching %s…", startUrl);
    Scraper.SiteMetadata metadata = Scraper.scrape(startUrl);
    System.out.println(metadata);

    // Collect bookmarkable links from likely navigation links on the page.
    JSONArray bookmarks = new JSONArray();
    for (Scraper.Endpoint bookmark : metadata.bookmarks) {
      Log.i("Adding BOOKMARK: %s", bookmark.url);
      bookmarks.put(new JSONObject()
          .put(JSONConstants.Fields.URL, bookmark.url)
          .put(JSONConstants.Fields.NAME, bookmark.title));
    }
    if (bookmarks.length() > 0) {
      root.put(JSONConstants.Roles.BOOKMARKS, bookmarks);
    }

    // Collect feed URLs.
    JSONArray feeds = new JSONArray();
    for (Scraper.Endpoint feed : metadata.feeds) {
      Log.i("Adding FEED: %s", feed.url);
      feeds.put(new JSONObject()
          .put(JSONConstants.Fields.URL, feed.url)
          .put(JSONConstants.Fields.NAME, feed.title));
    }
    if (feeds.length() > 0) {
      root.put(JSONConstants.Roles.FEEDS, feeds);
    }

    // If this site has a related Android app, we can grab the ID automatically too.
    JSONArray relatedApps = new JSONArray();
    for (String appId : metadata.relatedApps) {
      Log.i("Adding Related App: %s", appId);
      relatedApps.put(new JSONObject()
          .put(JSONConstants.Fields.PLATFORM, JSONConstants.Values.PLAY)
          .put(JSONConstants.Fields.URL, PLAY_STORE_URL_TEMPLATE + appId)
          .put(JSONConstants.Fields.ID, appId));
    }
    if (relatedApps.length() > 0) {
      root.put(JSONConstants.Fields.RELATED_APPLICATIONS, relatedApps);
    }

    // Put the icon JSON entry even if we don’t manage to fetch an icon successfully.
    // This way, we can avoid additional typing, and the validator will check for the presence
    // of the file anyway (and fail as expected).
    root.put(JSONConstants.Fields.ICONS, new JSONArray().put(
        new JSONObject().put(JSONConstants.Fields.SRC, FilePaths.ICON_FILENAME)));

    // TODO: Fetch favicon or apple-touch-icon.
    File iconFile = new File(liteAppDirectoryRoot, FilePaths.ICON_FILENAME);
    if (!iconFile.exists() && metadata.iconUrl != null && !metadata.iconUrl.isEmpty()) {
      Log.i("Fetching icon from %s…", metadata.iconUrl);
      URL icon = new URL(metadata.iconUrl);
      try (InputStream inputStream = icon.openStream()) {
        Files.copy(inputStream, iconFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        e.printStackTrace();
        // But still continue with the rest of the manifest generation.
      }
    }

    // Insert a placeholder for theme_color and secondary_color so we don’t have to
    // type it in manually, but put invalid values so that the validator will catch it
    // in case we forget to replace with valid values.
    root.put(JSONConstants.Fields.THEME_COLOR, "#");
    root.put(JSONConstants.Fields.SECONDARY_COLOR, "#");

    // Extract the color from the icon (either newly downloaded, or from existing icon).
    if (iconFile.exists()) {
      ColorExtractor.Color themeColor = ColorExtractor.getDominantColor(ImageIO.read(iconFile));
      if (themeColor != null) {
        // Overwrite the dummy values already inserted, if we are able to extract real values.
        root.put(JSONConstants.Fields.THEME_COLOR, themeColor.toString());
        root.put(JSONConstants.Fields.SECONDARY_COLOR, themeColor.darken(0.9f).toString());
      }
    }

    // Write the output manifest.
    try (PrintWriter writer = new PrintWriter(manifestJson)) {
      writer.print(root.toString(2));
    }
    return true;
  }

  public static void main(String[] arguments) {
    CommandLineParser parser = new DefaultParser();
    Options options = new Options()
        .addOption(Option.builder("u").required(true).hasArg(true)
            .longOpt(OPTION_URL)
            .argName("https://example.com")
            .desc("URL to create a Lite App for")
            .build())
        .addOption(Option.builder("t").required(true).hasArg(true)
            .longOpt(OPTION_TITLE)
            .argName("Example")
            .desc("Title of Lite App")
            .build());
    try {
      // The Gradle wrapper makes it hard to pass spaces within arguments, so allow users
      // to type in underscores instead of spaces, and we strip them out here. This is simpler
      // than trying to parse the parameters in Groovy/Gradle, so we chose this slightly-hacky
      // approach.
      CommandLine command = parser.parse(options, arguments);
      boolean success = Scaffolder.createScaffolding(
          command.getOptionValue(OPTION_URL),
          command.getOptionValue(OPTION_TITLE).replaceAll("_", " "));
      if (!success) {
        System.exit(1);
      }

    } catch (ParseException e) {
      final PrintWriter writer = new PrintWriter(System.out);
      new HelpFormatter().printHelp("Scaffolder", options);
      writer.flush();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
