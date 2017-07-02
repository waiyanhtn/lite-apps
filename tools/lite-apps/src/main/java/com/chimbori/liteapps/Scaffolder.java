package com.chimbori.liteapps;

import com.chimbori.common.ColorExtractor;
import com.chimbori.common.FileUtils;
import com.chimbori.common.Log;
import com.chimbori.hermitcrab.schema.manifest.Icon;
import com.chimbori.hermitcrab.schema.manifest.Manifest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * Generates a skeleton Lite App with as many fields pre-filled as possible.
 */
class Scaffolder {
  private static final String MANIFEST_URL_TEMPLATE = "https://hermit.chimbori.com/lite-apps/%s.hermit";

  private static final String LANG_EN = "en";

  private static final String COMMAND_LINE_OPTION_URL = "url";
  private static final String COMMAND_LINE_OPTION_NAME = "name";

  /**
   * The library.json file (containing metadata about all Lite Apps) is used as the basis for
   * generating scaffolding for the Lite App manifests.
   */
  private static void createScaffolding(String startUrl, String appName) throws IOException {
    File liteAppDirectoryRoot = new File(FilePaths.SRC_ROOT_DIR, appName);
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    Manifest manifest;
    File manifestJsonFile = new File(liteAppDirectoryRoot, FilePaths.MANIFEST_JSON_FILE_NAME);
    // If the manifest.json exists, read it before modifying, else create a new JSON object.
    if (manifestJsonFile.exists()) {
      manifest = Manifest.fromGson(gson, new FileReader(manifestJsonFile));
    } else {
      Log.i("Creating new Lite App %s", appName);
      // Create the root directory if it doesn’t exist yet.
      liteAppDirectoryRoot.mkdirs();

      // Scrape the Web looking for RSS & Atom feeds, theme colors, and site metadata.
      Log.i("Fetching %s…", startUrl);
      manifest = Scraper.scrape(startUrl);
      System.out.println(manifest);

      // Constant fields, same for all apps.
      manifest.manifest_version = 1;
      manifest.lang = LANG_EN;

      // Fields that can be populated from the data provided on the command-line.
      manifest.name = appName;
      manifest.start_url = startUrl;
      manifest.manifest_url = String.format(MANIFEST_URL_TEMPLATE, URLEncoder.encode(appName, "UTF-8").replace("+", "%20"));

      // Empty fields that must be manually populated.
      manifest.priority = 10;
      manifest.tags = new ArrayList<>(Arrays.asList(new String[]{"TODO"} ));

      // Put the icon JSON entry even if we don’t manage to fetch an icon successfully.
      // This way, we can avoid additional typing, and the validator will check for the presence
      // of the file anyway (and fail as expected).
      manifest.addIcon(new Icon(FilePaths.ICON_FILENAME));
    }

    // TODO: Fetch favicon or apple-touch-icon.
    File iconFile = new File(liteAppDirectoryRoot, FilePaths.ICON_FILENAME);
    if (!iconFile.exists() &&
        manifest.icons.get(0).src != null &&
        !manifest.icons.get(0).src.isEmpty()) {
      Log.i("Fetching icon from %s…", manifest.icons.get(0).src);
      URL icon = new URL(manifest.icons.get(0).src);
      try (InputStream inputStream = icon.openStream()) {
        Files.copy(inputStream, iconFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        e.printStackTrace();
        // But still continue with the rest of the manifest generation.
      }
    }

    // Extract the color from the icon (either newly downloaded, or from existing icon).
    if (iconFile.exists()) {
      ColorExtractor.Color themeColor = ColorExtractor.getDominantColor(ImageIO.read(iconFile));
      if (themeColor != null) {
        // Overwrite the dummy values already inserted, if we are able to extract real values.
        manifest.theme_color = themeColor.toString();
        manifest.secondary_color = themeColor.darken(0.9f).toString();
      } else {
        // Insert a placeholder for theme_color and secondary_color so we don’t have to
        // type it in manually, but put invalid values so that the validator will catch it
        // in case we forget to replace with valid values.
        manifest.theme_color = "#";
        manifest.secondary_color = "#";
      }
    } else {
      // Insert a placeholder for theme_color and secondary_color so we don’t have to
      // type it in manually, but put invalid values so that the validator will catch it
      // in case we forget to replace with valid values.
      manifest.theme_color = "#";
      manifest.secondary_color = "#";
    }

    // Write the output manifest.
    FileUtils.writeFile(manifestJsonFile, manifest.toJson(gson));
  }

  public static void main(String[] arguments) {
    CommandLineParser parser = new DefaultParser();
    Options options = new Options()
        .addOption(Option.builder("u").required(true).hasArg(true)
            .longOpt(COMMAND_LINE_OPTION_URL)
            .argName("https://example.com")
            .desc("URL to create a Lite App for")
            .build())
        .addOption(Option.builder("t").required(true).hasArg(true)
            .longOpt(COMMAND_LINE_OPTION_NAME)
            .argName("Example")
            .desc("Name of Lite App")
            .build());
    try {
      // The Gradle wrapper makes it hard to pass spaces within arguments, so allow users
      // to type in underscores instead of spaces, and we strip them out here. This is simpler
      // than trying to parse the parameters in Groovy/Gradle, so we chose this slightly-hacky
      // approach.
      CommandLine command = parser.parse(options, arguments);
      Scaffolder.createScaffolding(
          command.getOptionValue(COMMAND_LINE_OPTION_URL),
          command.getOptionValue(COMMAND_LINE_OPTION_NAME).replaceAll("_", " "));

    } catch (ParseException e) {
      final PrintWriter writer = new PrintWriter(System.out);
      new HelpFormatter().printHelp("Scaffolder", options);
      writer.flush();
      System.exit(1);

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
