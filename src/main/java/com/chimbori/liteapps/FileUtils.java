package com.chimbori.liteapps;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
  private static final int BUFFER_SIZE = 8192;

  public static boolean zip(File rootDir, File zipFile) {
    zipFile.getParentFile().mkdirs();  // In case the target directory doesn’t exist.
    try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
      for (File containedFile : rootDir.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return !pathname.getName().equals(".DS_Store");
        }
      })) {
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
      System.out.println("- " + zipEntryName);
      byte[] buffer = new byte[BUFFER_SIZE];
      try (FileInputStream fis = new FileInputStream(file)) {
        zipOutputStream.putNextEntry(new ZipEntry(zipEntryName));
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
}
