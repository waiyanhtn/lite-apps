package com.chimbori.liteapps;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;
import com.eclipsesource.json.WriterConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.fail;

/**
 * Uses a strict JSON parser to assert that the JSON is valid in the strictest sense.
 */
@RunWith(Parameterized.class)
public class StrictJSONParseTest extends ParameterizedLiteAppTest {
  public StrictJSONParseTest(File liteApp) {
    super(liteApp);
  }

  @Test
  public void testParseJSONStrictlyAndCheckWellFormed() throws IOException {
    Files.walkFileTree(liteApp.toPath(), new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() && file.toFile().getName().endsWith(".json")) {
          assertJsonIsWellFormed(file.toFile());
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
        fail(e.getMessage());
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void assertJsonIsWellFormed(File file) throws IOException {
    try {
      // Use a stricter parser than {@code JSONObject}, so we can catch issues such as
      // extra commas after the last element.
      JsonValue manifest = Json.parse(FileUtils.readFully(new FileInputStream(file)));
      // Re-indent the <b>source file</b> by saving the JSON back to the same file.
      FileUtils.writeFile(file, manifest.toString(WriterConfig.PRETTY_PRINT));
    } catch (ParseException e) {
      fail(String.format("%s: %s", file.getPath(), e.getMessage()));
    }
  }
}
