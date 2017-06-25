package com.chimbori.common;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileUtils {
  /**
   * The project root directory cannot be hard-coded in the code because it can and will be
   * different in different environments, e.g. local runs, continuous environments, etc.
   * Gradle also presumes a different root directory when launching a task for the root-level
   * project (:test or :check) versus the child projects (:blocklists:test or :blocklists:check).
   *
   * Using the ClassLoader offers us the most hermetic way of determining the correct paths.
   */
  public static File PROJECT_ROOT = null;
  static {
    try {
      PROJECT_ROOT = new File(new File(
          ClassLoader.getSystemResource(".").toURI()), "../../../../../").getCanonicalFile();
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }
  }

  private static final int BUFFER_SIZE = 8192;

  private static final OkHttpClient client = new OkHttpClient();

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
    file.getParentFile().mkdirs();
    System.out.println("Writing: " + file.getCanonicalPath());
    try (FileOutputStream fout = new FileOutputStream(file)) {
      fout.write(content.getBytes());
    }
  }

  public static String fetch(String url) throws IOException {
    System.out.println("Fetching: " + url);
    Request request = new Request.Builder().url(url).build();
    Response response = client.newCall(request).execute();
    return response.body().string();
  }
}
