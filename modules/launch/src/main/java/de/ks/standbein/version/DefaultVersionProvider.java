/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.standbein.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class DefaultVersionProvider implements VersionProvider {
  public static final String APP_VERSION_KEY = "app.version";

  private static final Logger log = LoggerFactory.getLogger(DefaultVersionProvider.class);
  private final URL manifestUrl;
  private File versionFile;

  @Inject
  public DefaultVersionProvider(@ManifestMfUrl URL manifestUrl, @VersionFile File versionFile) {
    this.manifestUrl = manifestUrl;
    this.versionFile = versionFile;
  }

  @Override
  public Optional<Integer> getLastVersion() {
    try (FileInputStream stream = new FileInputStream(versionFile)) {
      Properties properties = new Properties();
      properties.load(stream);
      String value = properties.getProperty(APP_VERSION_KEY, "-1");
      return Optional.of(Integer.valueOf(value));
    } catch (IOException e) {
      log.warn("No {} present, will assume version -1", versionFile);
      return Optional.empty();
    }
  }

  @Override
  public void writeLastVersion(int version) {
    if (!versionFile.exists()) {
      try {
        versionFile.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    try (FileOutputStream fileOutputStream = new FileOutputStream(versionFile)) {
      Properties properties = new Properties();
      properties.setProperty(APP_VERSION_KEY, String.valueOf(version));
      properties.store(fileOutputStream, "42");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getCurrentVersion() {
    String version = getManifestInfo("Implementation-Version");
    if (version == null) {
      return 0;
    } else {
      int indexOf = version.lastIndexOf("-");
      if (indexOf > 0) {
        version = version.substring(0, indexOf);
      }
      version = version.replaceAll("\\.", "");
      return Integer.valueOf(version);
    }
  }

  @Override
  public String getVersionString() {
    String version = getManifestInfo("Implementation-Version");
    return version;
  }

  public String getManifestInfo(String key) {
    try {
      InputStream is = manifestUrl.openStream();
      if (is != null) {
        Manifest manifest = new Manifest(is);
        Attributes mainAttribs = manifest.getMainAttributes();
        String value = mainAttribs.getValue(key);
        log.debug("From {} {}: {}", manifestUrl, key, value);
        return value;
      } else {
        log.warn("No manifest.mf in {}", manifestUrl);
      }
    } catch (Exception e) {
      log.error("Could not open manifest.mf from {}", manifestUrl, e);
    }
    return null;
  }

//
//  private URL discoverManifestUrl() {
//    URL ownerLocation = owner.getProtectionDomain().getCodeSource().getLocation();
//    log.info("Using owner location: '{}'", ownerLocation.getFile());
//
//    ArrayList<URL> urlCandidates = new ArrayList<>();
//    Enumeration resEnum;
//    try {
//      resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
//      while (resEnum.hasMoreElements()) {
//        URL url = (URL) resEnum.nextElement();
//        urlCandidates.add(url);
//        if (url.getFile().contains(ownerLocation.getFile())) {
//          log.info("Found URL to read manifest.mf {}", url);
//          return url;
//        }
//      }
//    } catch (IOException e1) {
//      log.error("Unknown exception ", e1);
//    }
//    log.info("Found no manifest.mf url. Candidates: {}", urlCandidates.stream().map(url -> "\n\t" + url.getFile()).collect(Collectors.toList()));
//    return null;
//  }
}
