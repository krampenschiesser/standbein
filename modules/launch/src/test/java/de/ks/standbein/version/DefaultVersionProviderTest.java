package de.ks.standbein.version;

import com.google.common.base.StandardSystemProperty;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

public class DefaultVersionProviderTest {
  protected boolean isWindows = StandardSystemProperty.OS_NAME.value().toLowerCase(Locale.ROOT).contains("win");
  private DefaultVersionProvider provider;
  private Path manifestPath;
  private Path versionPath;

  @Before
  public void setUp() throws Exception {
    String tempDir = StandardSystemProperty.JAVA_IO_TMPDIR.value();
    manifestPath = Paths.get(tempDir, "MyManifest.mf");
    deleteIfExists(manifestPath);
    versionPath = Paths.get(tempDir, "MyVersion.txt");
    deleteIfExists(versionPath);

    Files.write(manifestPath, Arrays.asList(//
      "Manifest-Version: 1.0",//
      "Implementation-Title: testing",//
      "Implementation-Version: 0.2.3-SNAPSHOT",//
      "Implementation-Vendor: krampenschiesser"//
    ));

    URL manifestMfUrl = manifestPath.toUri().toURL();
    File versionFile = versionPath.toFile();

    provider = new DefaultVersionProvider(manifestMfUrl, versionFile);
  }

  private void deleteIfExists(Path path) throws IOException, InterruptedException {
    int maxRetry = isWindows ? 5 : 1;
    for (int retry = 0; retry < maxRetry; retry++) {
      try {

        if (Files.exists(path) && path.toFile().exists()) {
          Files.delete(path);
        }
      } catch (FileSystemException e) {
        if (isWindows) {
          if (retry < maxRetry - 1) {
            Thread.sleep(1050);
          } else {
            throw e;
          }
        }

      }
    }
  }

  @Test
  public void testWriteVersion() throws Exception {
    provider.writeLastVersion(420);
    assertTrue(Files.exists(versionPath));
    List<String> lines = Files.readAllLines(versionPath);
    assertEquals(3, lines.size());
    assertEquals(1, lines.stream().filter(l -> !l.startsWith("#")).count());
    assertEquals(DefaultVersionProvider.APP_VERSION_KEY + "=420", lines.get(2));
  }

  @Test
  public void testNoLastVersion() throws Exception {
    Optional<Integer> lastVersion = provider.getLastVersion();
    assertFalse(lastVersion.isPresent());
  }

  @Test
  public void testReadLastVersion() throws Exception {
    Files.write(versionPath, Collections.singleton(DefaultVersionProvider.APP_VERSION_KEY + " = 142"));
    int lastVersion = provider.getLastVersion().get();
    assertEquals(142, lastVersion);
  }

  @Test
  public void testReadFromMetaInf() throws Exception {
    int currentVersion = provider.getCurrentVersion();
    assertEquals(23, currentVersion);
  }

  @Test
  public void testGetVersionString() throws Exception {
    String versionString = provider.getVersionString();
    assertEquals("0.2.3-SNAPSHOT", versionString);
  }
}