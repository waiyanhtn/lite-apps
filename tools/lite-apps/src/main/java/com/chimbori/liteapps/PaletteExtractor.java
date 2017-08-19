package com.chimbori.liteapps;

import com.chimbori.common.ColorExtractor;
import com.chimbori.common.FileUtils;
import com.chimbori.hermitcrab.schema.common.GsonInstance;
import com.chimbori.hermitcrab.schema.manifest.Manifest;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PaletteExtractor {
  public static void extractPaletteIfMissing() throws IOException {
    Gson gson = GsonInstance.getPrettyPrinter();

    File[] liteAppDirs = FilePaths.SRC_ROOT_DIR.listFiles();
    for (File liteAppDirectory : liteAppDirs) {
      if (!liteAppDirectory.isDirectory()) {
        continue; // Probably a temporary file, like .DS_Store.
      }

      String appName = liteAppDirectory.getName();
      File manifestJsonFile = new File(liteAppDirectory, FilePaths.MANIFEST_JSON_FILE_NAME);
      if (!manifestJsonFile.exists()) {
        throw new MissingManifestException(appName);
      }

      // Create an entry for this Lite App to be put in the directory index file.
      Manifest manifest = gson.fromJson(new FileReader(manifestJsonFile), Manifest.class);

      if (manifest.themeColor.equals("#") || manifest.secondaryColor.equals("#")) {
        System.err.println("manifest: " + manifest.name);

        File iconsDirectory = new File(liteAppDirectory, FilePaths.ICONS_DIR_NAME);
        iconsDirectory.mkdirs();
        File iconFile = new File(iconsDirectory, FilePaths.FAVICON_FILENAME);

        // Extract the color from the icon (either newly downloaded, or from existing icon).
        if (iconFile.exists()) {
          ColorExtractor.Color themeColor = ColorExtractor.getDominantColor(ImageIO.read(iconFile));
          if (themeColor != null) {
            // Overwrite the dummy values already inserted, if we are able to extract real values.
            manifest.themeColor = themeColor.toString();
            manifest.secondaryColor = themeColor.darken(0.9f).toString();

            FileUtils.writeFile(manifestJsonFile, gson.toJson(manifest));

          }
        }
      }
    }
  }

  public static void main(String[] arguments) {
    try {
      extractPaletteIfMissing();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
