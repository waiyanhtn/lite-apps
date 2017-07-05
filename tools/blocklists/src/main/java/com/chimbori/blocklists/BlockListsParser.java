package com.chimbori.blocklists;

import com.chimbori.blocklists.json.AppManifestBlockList;
import com.chimbori.blocklists.json.BlockListsIndex;
import com.chimbori.common.FileUtils;
import com.chimbori.hermitcrab.schema.common.GsonInstance;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses a meta-list of block-lists, fetches the original blocklists from various remote URLs,
 * and combines them into a single JSON file suitable for consumption in Hermit.
 */
public class BlockListsParser {
  private static final List<String> WHITELISTED_SUBSTRINGS = ImmutableList.of(
      "youtube"
  );

  private static final String LOCAL_IP_V4 = "127.0.0.1";
  private static final String LOCAL_IP_V4_ALT = "0.0.0.0";
  private static final String LOCAL_IP_V6 = "::1";
  private static final String LOCALHOST = "localhost";
  private static final String COMMENT = "#";
  private static final String TAB = "\t";
  private static final String SPACE = " ";
  private static final String EMPTY = "";

  /**
   * Downloads all the meta-lists from index.json and saves them locally.
   */
  public static void downloadFromSources() throws IOException {
    BlockListsIndex blockListsIndex = readIndexJsonWithGson();
    for (BlockListsIndex.BlockList blockList : blockListsIndex.blocklists) {
      File blockListDirectory = new File(FilePaths.SRC_ROOT_DIR, blockList.blocklist);
      blockListDirectory.mkdirs();
      for (BlockListsIndex.BlockList.Source source : blockList.sources) {
        // A blank URL means it’s a local file, so no need to fetch it from a remote server.
        if (source.url != null && !source.url.isEmpty()) {
          FileUtils.writeFile(new File(blockListDirectory, source.name), FileUtils.fetch(source.url));
        }
      }
    }
  }

  /**
   * Package multiple blocklists into a single JSON file, as specified in index.json.
   */
  public static void packageBlockLists(boolean shouldMinify) throws IOException {
    System.out.println(new File(".").getAbsolutePath());

    BlockListsIndex blockListsIndex = readIndexJsonWithGson();

    for (BlockListsIndex.BlockList blockList : blockListsIndex.blocklists) {
      Set<String> hosts = new HashSet<>();

      File blockListDirectory = new File(FilePaths.SRC_ROOT_DIR, blockList.blocklist);
      blockListDirectory.mkdirs();

      for (BlockListsIndex.BlockList.Source source : blockList.sources) {
        // Since we don’t want to download the blocklists to keep the test hermetic, and we want to
        // still run the test on blocklists that are uploaded to the repo (i.e. first-party owned),
        // we skip adding hosts from a file if it doesn’t already exist.
        File hostsList = new File(blockListDirectory, source.name);
        if (hostsList.exists()) {
          parseBlockList(source.name, new FileInputStream(hostsList), hosts);
        }
      }

      writeToDisk(FilePaths.OUT_ROOT_DIR, blockList.name, hosts, shouldMinify);
      hosts.clear();  // Empty the list before writing each one.
    }
  }

  private static void parseBlockList(String sourceName, InputStream inputStream, Set<String> hosts) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      int hostsAdded = 0;
      while ((line = reader.readLine()) != null) {
        if (!line.isEmpty() && !line.startsWith(COMMENT)) {
          line = line.replace(LOCAL_IP_V4, EMPTY)
              .replace(LOCAL_IP_V4_ALT, EMPTY)
              .replace(LOCAL_IP_V6, EMPTY)
              .replace(TAB, EMPTY);
          int comment = line.indexOf(COMMENT);
          if (comment >= 0) {
            line = line.substring(0, comment);
          }
          line = line.trim();
          if (!line.isEmpty() && !line.equals(LOCALHOST)) {
            while (line.contains(SPACE)) {
              int space = line.indexOf(SPACE);
              if (addHostIfNotNullOrWhiteListed(line.substring(0, space), hosts)) {
                hostsAdded++;
              }
              line = line.substring(space, line.length()).trim();
            }
            if (addHostIfNotNullOrWhiteListed(line.trim(), hosts)) {
              hostsAdded++;
            }
          }
        }
      }
      System.out.println(String.format("%s: %d", sourceName, hostsAdded));

    } catch (IOException e) {
      e.printStackTrace();

    } finally {
      if (reader == null)
        return;
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static boolean addHostIfNotNullOrWhiteListed(String host, Set<String> hosts) {
    if (host != null && !isHostWhitelisted(host)) {
      hosts.add(host.trim());
      return true;
    }
    return false;
  }

  private static void writeToDisk(File rootDirectory, String fileName, Set<String> hosts, boolean shouldMinify) throws IOException {
    String[] hostsArray = hosts.toArray(new String[0]);
    Arrays.sort(hostsArray);

    AppManifestBlockList appManifestBlockList = new AppManifestBlockList();
    appManifestBlockList.name = fileName;
    appManifestBlockList.updated = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
    appManifestBlockList.hosts = hostsArray;

    System.out.println(String.format("Wrote %d hosts.\n", hosts.size()));

    Gson gson = shouldMinify ? GsonInstance.getMinifier() : GsonInstance.getPrettyPrinter();

    FileUtils.writeFile(new File(rootDirectory, fileName), gson.toJson(appManifestBlockList));
  }

  /**
   * In order to allow Hermit to continue to be distributed via Google Play, certain ads domains
   * cannot be blocked. We apologize for the inconvenience, but this is not in our control.
   */
  private static boolean isHostWhitelisted(String host) {
    for (String whitelistedSubstring : WHITELISTED_SUBSTRINGS) {
      if (host.contains(whitelistedSubstring)) {
        return true;
      }
    }
    return false;
  }

  private static BlockListsIndex readIndexJsonWithGson() throws IOException {
    return GsonInstance.getMinifier().fromJson(
        FileUtils.readFully(new FileInputStream(FilePaths.SRC_BLOCK_LISTS_JSON)),
        BlockListsIndex.class);
  }
}
