package com.chimbori.liteapps;

import java.io.File;
import java.io.FileFilter;

/**
 * Packages all Lite Apps into their corresponding .hermit packages.
 * Does not validate each Lite App prior to packaging: it is assumed that this is run on a green
 * build which has already passed all validation tests.
 */
public class ManifestPackager {
  public static void main(String[] arguments) {
    if (!packageAllManifests(new File("lite-apps/"))) {
      System.exit(1);
    }
  }

  public static boolean packageAllManifests(File liteAppsDirectory) {
    boolean allSuccessful = true;
    for (File liteApp : liteAppsDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.isDirectory();
      }
    })) {
      allSuccessful = allSuccessful && packageManifest(liteApp);
    }
    return allSuccessful;
  }

  public static boolean packageManifest(File liteAppRoot) {
    File liteAppZipped = new File("bin/", liteAppRoot.getName() + ".hermit");
    System.out.println(liteAppZipped.getName());
    FileUtils.zip(liteAppRoot, liteAppZipped);
    System.out.println();
    return true;
  }
}
