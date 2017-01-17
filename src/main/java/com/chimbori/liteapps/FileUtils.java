package com.chimbori.liteapps;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class FileUtils {

  private static final int BUFFER_SIZE = 8192;

  private static OkHttpClient client = new OkHttpClient();

  public static final String MANIFEST_JSON_FILE_NAME = "manifest.json";
  public static final String LOCALES_DIR_NAME = "_locales";
  public static final String MESSAGES_JSON_FILE_NAME = "messages.json";
  private static final String INDEX_JSON_FILE_NAME = "index.json";
  private static final String LITE_APPS_JSON_FILE_NAME = "lite-apps.json";
  public static final String ICON_FILENAME = "icon.png";

  public static final File SRC_ROOT_DIR = new File("lite-apps/");
  public static final File OUT_ROOT_DIR = new File("bin/");
  private static final File OUT_DATA_DIR = new File(OUT_ROOT_DIR, "_data/");
  public static final File OUT_LITE_APPS_JSON = new File(OUT_DATA_DIR, LITE_APPS_JSON_FILE_NAME);
  public static final File OUT_LITE_APPS_DIR = new File(OUT_ROOT_DIR, "lite-apps/");

  public static final File SRC_INDEX_JSON = new File(SRC_ROOT_DIR, INDEX_JSON_FILE_NAME);

  public static final File BLOCK_LISTS_ROOT_DIR = new File("blocklists/");
  public static final File BLOCK_LISTS_INDEX_JSON = new File(FileUtils.BLOCK_LISTS_ROOT_DIR, INDEX_JSON_FILE_NAME);

  static {
    OUT_ROOT_DIR.mkdirs();
    OUT_DATA_DIR.mkdirs();
    OUT_LITE_APPS_DIR.mkdirs();
  }

  public static boolean zip(File rootDir, File zipFile) {
    try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
      out.setLevel(9);
      for (File containedFile : rootDir.listFiles(pathname -> !pathname.getName().equals(".DS_Store"))) {
        // Add files contained under the root directory, instead of the root directory itself,
        // so that the individual files appear at the root of the zip file instead of one directory down.
        addFileToZip(out, containedFile, "" /* parentDirectoryName */);
      }
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private static void addFileToZip(ZipOutputStream zipOutputStream, File file, String parentDirectoryName) throws IOException {
    if (file == null || !file.exists()) {
      return;
    }

    String zipEntryName = file.getName();
    if (parentDirectoryName != null && !parentDirectoryName.isEmpty()) {
      zipEntryName = parentDirectoryName + "/" + file.getName();
    }

    if (file.isDirectory()) {
      for (File containedFile : file.listFiles()) {
        addFileToZip(zipOutputStream, containedFile, zipEntryName);
      }
    } else {
      byte[] buffer = new byte[BUFFER_SIZE];
      try (FileInputStream fis = new FileInputStream(file)) {
        ZipEntry zipEntry = new ZipEntry(zipEntryName);
        // Intentionally set the last-modified date to the epoch, so running the zip command
        // multiple times on the same (unchanged) source does not result in a different (binary)
        // zip file everytime.
        FileTime epochTime = FileTime.fromMillis(0);
        zipEntry.setCreationTime(epochTime);
        zipEntry.setLastModifiedTime(epochTime);
        zipEntry.setLastAccessTime(epochTime);
        zipOutputStream.putNextEntry(zipEntry);
        int length;
        while ((length = fis.read(buffer)) > 0) {
          zipOutputStream.write(buffer, 0, length);
        }
      } finally {
        zipOutputStream.closeEntry();
      }
    }
  }

  public static String readFully(InputStream inputStream) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[BUFFER_SIZE];
    int length;
    while ((length = inputStream.read(buffer)) != -1) {
      baos.write(buffer, 0, length);
    }
    return baos.toString("UTF-8");
  }

  public static void writeFile(File file, String content) throws IOException {
    try (FileOutputStream fout = new FileOutputStream(file)) {
      fout.write(content.getBytes());
    }
  }

  public static String fetch(String url) throws IOException {
    Request request = new Request.Builder().url(url).build();
    Response response = client.newCall(request).execute();
    return response.body().string();
  }
}
