package com.chimbori.liteapps;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses a meta-list of block-lists, fetches the original blocklists from various remote URLs,
 * and combines them into a single JSON file suitable for consumption in Hermit.
 */
public class BlockListsParser {
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
  public static boolean downloadFromSources() throws IOException {
    JSONArray blockListSources = new JSONArray(FileUtils.readFully(new FileInputStream(FileUtils.BLOCK_LISTS_INDEX_JSON)));

    for (int i = 0; i < blockListSources.length(); i++) {
      JSONObject blockList = blockListSources.getJSONObject(i);
      String blockListName = blockList.getString(JSONConstants.Fields.BLOCK_LIST);
      File blockListDirectory = new File(FileUtils.BLOCK_LISTS_ROOT_DIR, blockListName);
      blockListDirectory.mkdirs();

      JSONArray sources = blockList.getJSONArray(JSONConstants.Fields.SOURCES);
      for (int j = 0; j < sources.length(); j++) {
        JSONObject source = sources.getJSONObject(j);
        String sourceName = source.getString(JSONConstants.Fields.NAME);

        // A blank URL means it’s a local file, so no need to fetch it from a remote server.
        String sourceUrl = source.optString(JSONConstants.Fields.URL);
        if (sourceUrl != null && !sourceUrl.isEmpty()) {
          FileUtils.writeFile(new File(blockListDirectory, sourceName), FileUtils.fetch(sourceUrl));
        }
      }
    }

    return true;
  }

  /**
   * Package multiple blocklists into a single JSON file, as specified in index.json.
   */
  public static boolean packageBlockLists() throws IOException {
    JSONArray blockListSources = new JSONArray(FileUtils.readFully(new FileInputStream(FileUtils.BLOCK_LISTS_INDEX_JSON)));
    for (int i = 0; i < blockListSources.length(); i++) {
      Set<String> hosts = new HashSet<>();

      JSONObject blockList = blockListSources.getJSONObject(i);
      String blockListName = blockList.getString(JSONConstants.Fields.BLOCK_LIST);
      String fileName = blockList.getString(JSONConstants.Fields.NAME);
      File blockListDirectory = new File(FileUtils.BLOCK_LISTS_ROOT_DIR, blockListName);
      blockListDirectory.mkdirs();

      JSONArray sources = blockList.getJSONArray(JSONConstants.Fields.SOURCES);
      for (int j = 0; j < sources.length(); j++) {
        JSONObject source = sources.getJSONObject(j);
        String sourceName = source.getString(JSONConstants.Fields.NAME);

        // Since we don’t want to download the blocklists to keep the test hermetic, and we want to
        // still run the test on blocklists that are uploaded to the repo (i.e. first-party owned),
        // we skip adding hosts from a file if it doesn’t already exist.
        File hostsList = new File(blockListDirectory, sourceName);
        if (hostsList.exists()) {
          parseBlockList(sourceName, new FileInputStream(hostsList), hosts);
        }
      }

      writeToDisk(FileUtils.BLOCK_LISTS_ROOT_DIR, fileName, hosts);
      hosts.clear();  // Empty the list before writing each one.
    }

    return true;
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
              if (addHostIfNotNull(line.substring(0, space), hosts)) {
                hostsAdded++;
              }
              line = line.substring(space, line.length()).trim();
            }
            if (addHostIfNotNull(line.trim(), hosts)) {
              hostsAdded++;
            }
          }
        }
      }
      System.err.println(String.format("%s: %d", sourceName, hostsAdded));

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

  private static boolean addHostIfNotNull(String host, Set<String> hosts) {
    if (host != null) {
      hosts.add(host.trim());
      return true;
    }
    return false;
  }

  private static void writeToDisk(File rootDirectory, String fileName, Set<String> hosts) throws IOException {
    String[] hostsArray = hosts.toArray(new String[0]);
    Arrays.sort(hostsArray);

    JSONObject outputFile = new JSONObject();
    outputFile.put(JSONConstants.Fields.NAME, fileName);
    outputFile.put(JSONConstants.Fields.UPDATED, new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()));
    JSONArray jsonHosts = new JSONArray();
    for (String host : hostsArray) {
      jsonHosts.put(host);
    }
    outputFile.put(JSONConstants.Fields.HOSTS, jsonHosts);

    System.err.println(String.format("Wrote %d hosts.\n", hosts.size()));

    FileOutputStream blockList = new FileOutputStream(new File(rootDirectory, fileName));
    blockList.write(outputFile.toString(2).getBytes());
    blockList.close();
  }
}
