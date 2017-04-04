package com.chimbori.liteapps;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

/**
 * Generates a skeleton Lite App with as many fields pre-filled as possible.
 */
class Scaffolder {
  /**
   * The lite-apps.json file (containing metadata about all Lite Apps) is used as the basis for
   * generating scaffolding for the Lite App manifests.
   */
  private static boolean createScaffolding(String startUrl, String appName) throws JSONException, IOException {
    // Create the root directory if it doesn’t exist yet.
    File liteAppDirectoryRoot = new File(FileUtils.SRC_ROOT_DIR, appName);
    if (!liteAppDirectoryRoot.exists()) {
      liteAppDirectoryRoot.mkdirs();
    } else {
      System.err.println(String.format("Lite App “%s” already exists.", appName));
      return false;
    }

    // If the manifest.json exists, read it before modifying, else create a new JSON object.
    JSONObject root = new JSONObject();

    // Constant fields, same for all apps.
    root.put(JSONConstants.Fields.MANIFEST_VERSION, 1);
    root.put(JSONConstants.Fields.LANG, JSONConstants.Values.EN);

    // Fields that can be populated from the data provided on the command-line.
    root.put(JSONConstants.Fields.NAME, appName);
    root.put(JSONConstants.Fields.START_URL, startUrl);
    root.put(JSONConstants.Fields.MANIFEST_URL, String.format("https://hermit.chimbori.com/lite-apps/%s.hermit",
        URLEncoder.encode(appName, "UTF-8").replace("+", "%20")));

    // Scrape the Web looking for RSS & Atom feeds, theme colors, and site metadata.
    System.out.println(String.format("Fetching %s…", startUrl));
    Scraper.SiteMetadata metadata = Scraper.scrape(startUrl);
    System.out.println(metadata);

    // Put the icon even if we don’t manage to fetch an icon successfully.
    // This way, we can avoid additional typing, and the validator will check for the presence
    // of the file anyway (and fail as expected).
    root.put(JSONConstants.Fields.ICONS, new JSONArray().put(new JSONObject().put(JSONConstants.Fields.SRC, FileUtils.ICON_FILENAME)));

    // TODO(manas): Fetch favicon or apple-touch-icon.
    if (metadata.iconUrl != null && !metadata.iconUrl.isEmpty()) {
      URL website = new URL(metadata.iconUrl);
      try (InputStream inputStream = website.openStream()) {
        Files.copy(inputStream, new File(liteAppDirectoryRoot, FileUtils.ICON_FILENAME).toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        e.printStackTrace();
        // But still continue with the rest of the manifest generation.
      }

      // Extract the color from the icon.
      File iconFile = new File(liteAppDirectoryRoot, FileUtils.ICON_FILENAME);
      if (iconFile.exists()) {
        ColorExtractor.Color themeColor = ColorExtractor.getDominantColor(ImageIO.read(iconFile));
        root.put(JSONConstants.Fields.THEME_COLOR, themeColor.toString());
        root.put(JSONConstants.Fields.SECONDARY_COLOR, themeColor.darken(0.9f).toString());
      }
    } else {
      // Insert a placeholder for theme_color and secondary_color so we don’t have to
      // type it in manually, but put invalid values so that the validator will catch it
      // in case we forget to replace with valid values.
      root.put(JSONConstants.Fields.THEME_COLOR, "#");
      root.put(JSONConstants.Fields.SECONDARY_COLOR, "#");
    }

    // Collect bookmarkable links from likely navigation links on the page.
    JSONArray bookmarks = new JSONArray();
    for (Scraper.Endpoint bookmark : metadata.bookmarks) {
      System.out.println("Including BOOKMARK: " + bookmark.url);
      bookmarks.put(new JSONObject()
          .put(JSONConstants.Fields.URL, bookmark.url)
          .put(JSONConstants.Fields.NAME, bookmark.title));
    }
    if (bookmarks.length() > 0) {
      root.put("hermit_bookmarks", bookmarks);
    }

    // Collect feed URLs.
    JSONArray feeds = new JSONArray();
    for (Scraper.Endpoint feed : metadata.feeds) {
      System.out.println("Including FEED: " + feed.url);
      feeds.put(new JSONObject()
          .put(JSONConstants.Fields.URL, feed.url)
          .put(JSONConstants.Fields.NAME, feed.title));
    }
    if (feeds.length() > 0) {
      root.put("hermit_feeds", feeds);
    }

    // If this site has a related Android app, we can grab the ID automatically too.
    JSONArray relatedApps = new JSONArray();
    for (String appId : metadata.relatedApps) {
      relatedApps.put(new JSONObject()
          .put(JSONConstants.Fields.PLATFORM, JSONConstants.Values.PLAY)
          .put(JSONConstants.Fields.URL, "https://play.google.com/store/apps/details?id=" + appId)
          .put(JSONConstants.Fields.ID, appId));
    }
    if (relatedApps.length() > 0) {
      root.put("related_applications", relatedApps);
    }

    // Write the output manifest.
    File manifestJson = new File(liteAppDirectoryRoot, FileUtils.MANIFEST_JSON_FILE_NAME);
    try (PrintWriter writer = new PrintWriter(manifestJson)) {
      writer.print(root.toString(2));
    }
    return true;
  }

  public static void main(String[] arguments) {
    CommandLineParser parser = new DefaultParser();
    Options options = new Options()
        .addOption(Option.builder("u").required(true).hasArg(true)
            .longOpt("url")
            .argName("https://example.com")
            .desc("URL to create a Lite App for")
            .build())
        .addOption(Option.builder("t").required(true).hasArg(true)
            .longOpt("title")
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
          command.getOptionValue("url"),
          command.getOptionValue("title").replaceAll("_", " "));
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
