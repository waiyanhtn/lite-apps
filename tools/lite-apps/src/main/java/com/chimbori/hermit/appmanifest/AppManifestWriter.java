package com.chimbori.hermit.appmanifest;

import com.chimbori.FilePaths;
import com.chimbori.common.FileUtils;
import com.chimbori.hermitcrab.schema.appmanifest.AppManifest;
import com.chimbori.hermitcrab.schema.appmanifest.AppVersion;
import com.chimbori.hermitcrab.schema.appmanifest.AssetArchive;
import com.chimbori.hermitcrab.schema.appmanifest.Manifest;
import com.chimbori.hermitcrab.schema.common.GsonInstance;
import com.chimbori.hermitcrab.schema.common.SchemaDate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Writes an app manifest with the provided values. This is a convenience writer class for the JSON
 * to avoid having to write (and then validate) JSON by hand.
 */
public class AppManifestWriter {

  /**
   * Static entry point into this class. It writes the current manifest as a minified JSON file
   * to the output bin/ directory.
   */
  public static void writeManifest() throws IOException {
    AppManifest appManifest = new AppManifest().manifest(createManifestForHardCodedVersion());
    FileUtils.writeFile(
        new File(FilePaths.OUT_APP_MANIFEST_DIR, FilePaths.APP_MANIFEST_FILE_NAME),
        GsonInstance.getMinifier().toJson(appManifest));
  }

  /**
   * Update this method for every release to include features for that release.
   */
  private static Manifest createManifestForHardCodedVersion() {
    Manifest manifest = createManifestWithDefaults();
    manifest.versions = new ArrayList<>();
    AppVersion version = new AppVersion()
        .track("production")
        .versionCode(100000)
        .os("android")
        .versionName("10.0.0")
        .released(new SchemaDate(2017, 8, 18))
        .minSdkVersion(19);

    version.features = new ArrayList<>();
    version.features.add("<b>Android O</b>: The version has no name, but new features are coming. Hermit is ready for Android O!");
    version.features.add("<b>Notification Channels</b>: Control every aspect of Lite App notifications through Android O’s centralized notification manager.");
    version.features.add("<b>Phishing and Malware Protection</b>: If any malware sites manage to get through Hermit’s Ad & Malware Blocker, then they will be automatically flagged by Google’s Malware Blocker");
    version.features.add("<b>Incognito Keyboard</b>: Any URLs or queries you type will be marked as private by your keyboard. Form data won’t be saved either.");
    version.features.add("Halo! Ciao! Now in Indonesian and Italian too.");
    version.features.add("<b>Cleaner Reader View</b>: Large images at the top, cleaner text formatting makes it easier to read.");
    version.features.add("<b>Alphabetical Sorting</b> for Lite Apps.");
    version.features.add("See the full list at https://hermit.chimbori.com/changes");
    manifest.versions.add(version);

    return manifest;
  }

  /**
   * Creates a manifest that has values that don’t depend on a specific release version.
   * When malware blocklists, fonts, or styles are updated, changes should be made here.
   */
  private static Manifest createManifestWithDefaults() {
    Manifest manifest = new Manifest();
    manifest.locale = "en";

    manifest.blocklists = new ArrayList<>();
    manifest.blocklists.add(new AssetArchive()
        .name("Adware and Malware")
        .updated(new SchemaDate(2017, 8, 16))
        .url("https://hermit.chimbori.com/app/adware-malware.json.zip"));

    manifest.fonts = new ArrayList<>();
    manifest.fonts.add(new AssetArchive()
        .name("Basic Fonts")
        .updated(new SchemaDate(2016, 12, 7))
        .url("https://hermit.chimbori.com/app/basic-fonts.zip"));

    manifest.styles = new ArrayList<>();
    manifest.styles.add(new AssetArchive()
        .name("Night Styles")
        .updated(new SchemaDate(2017, 1, 4))
        .url("https://hermit.chimbori.com/app/night-styles.zip"));

    return manifest;
  }
}
