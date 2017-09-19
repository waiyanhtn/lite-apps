package com.chimbori.liteapps;

import com.chimbori.FilePaths;
import com.chimbori.common.ColorExtractor;
import com.chimbori.common.FileUtils;
import com.chimbori.common.Log;
import com.chimbori.hermitcrab.schema.common.GsonInstance;
import com.chimbori.hermitcrab.schema.manifest.Manifest;

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

  private static final Integer CURRENT_MANIFEST_VERSION = 2;

  /**
   * The library.json file (containing metadata about all Lite Apps) is used as the basis for
   * generating scaffolding for the Lite App manifests.
   */
  private static void createScaffolding(String startUrl, String appName) throws IOException {
    File liteAppDirectoryRoot = new File(FilePaths.SRC_ROOT_DIR, appName);

    Manifest manifest;
    File manifestJsonFile = new File(liteAppDirectoryRoot, FilePaths.MANIFEST_JSON_FILE_NAME);
    // If the manifest.json exists, read it before modifying, else create a new JSON object.
    if (manifestJsonFile.exists()) {
      manifest = GsonInstance.getMinifier().fromJson(new FileReader(manifestJsonFile), Manifest.class);
    } else {
      Log.i("Creating new Lite App %s", appName);
      // Create the root directory if it doesn’t exist yet.
      liteAppDirectoryRoot.mkdirs();

      // Scrape the Web looking for RSS & Atom feeds, theme colors, and site metadata.
      Log.i("Fetching %s…", startUrl);
      manifest = Scraper.scrape(startUrl);
      System.out.println(manifest);

      // Constant fields, same for all apps.
      manifest.manifestVersion = CURRENT_MANIFEST_VERSION;
      manifest.lang = LANG_EN;

      // Fields that can be populated from the data provided on the command-line.
      manifest.name = appName;
      manifest.startUrl = startUrl;
      manifest.manifestUrl = String.format(MANIFEST_URL_TEMPLATE, URLEncoder.encode(appName, "UTF-8").replace("+", "%20"));

      // Empty fields that must be manually populated.
      manifest.priority = 10;
      manifest.tags = new ArrayList<>(Arrays.asList(new String[]{"TODO"} ));
    }

    // TODO: Fetch favicon or apple-touch-icon.
    String remoteIconUrl = manifest.icon;

    File iconsDirectory = new File(liteAppDirectoryRoot, FilePaths.ICONS_DIR_NAME);
    iconsDirectory.mkdirs();
    File iconFile = new File(iconsDirectory, FilePaths.FAVICON_FILENAME);

    if (!iconFile.exists() && manifest.icon != null && !manifest.icon.isEmpty()) {
      Log.i("Fetching icon from %s…", manifest.icon);
      URL iconUrl = new URL(remoteIconUrl);
      try (InputStream inputStream = iconUrl.openStream()) {
        Files.copy(inputStream, iconFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        e.printStackTrace();
        // But still continue with the rest of the manifest generation.
      }
    }

    // Put the icon JSON entry even if we don’t manage to fetch an icon successfully.
    // This way, we can avoid additional typing, and the validator will check for the presence
    // of the file anyway (and fail as expected).
    manifest.icon = FilePaths.FAVICON_FILENAME;

    // Extract the color from the icon (either newly downloaded, or from existing icon).
    if (iconFile.exists()) {
      ColorExtractor.Color themeColor = ColorExtractor.getDominantColor(ImageIO.read(iconFile));
      if (themeColor != null) {
        // Overwrite the dummy values already inserted, if we are able to extract real values.
        manifest.themeColor = themeColor.toString();
        manifest.secondaryColor = themeColor.darken(0.9f).toString();
      } else {
        // Insert a placeholder for theme_color and secondary_color so we don’t have to
        // type it in manually, but put invalid values so that the validator will catch it
        // in case we forget to replace with valid values.
        manifest.themeColor = "#";
        manifest.secondaryColor = "#";
      }
    } else {
      // Insert a placeholder for theme_color and secondary_color so we don’t have to
      // type it in manually, but put invalid values so that the validator will catch it
      // in case we forget to replace with valid values.
      manifest.themeColor = "#";
      manifest.secondaryColor = "#";
    }

    // Write the output manifest.
    FileUtils.writeFile(manifestJsonFile, GsonInstance.getPrettyPrinter().toJson(manifest));
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
