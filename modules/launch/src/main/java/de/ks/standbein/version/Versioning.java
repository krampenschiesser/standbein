/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.*;

public class Versioning {
  private static final Logger log = LoggerFactory.getLogger(Versioning.class);

  private final Collection<InitialImport> initialImports;
  private final Collection<VersionUpgrade> upgrades;
  private final VersionProvider versionProvider;

  @Inject
  public Versioning(Collection<InitialImport> initialImports, Collection<VersionUpgrade> upgrades, VersionProvider versionProvider) {
    this.initialImports = initialImports;
    this.upgrades = upgrades;
    this.versionProvider = versionProvider;
  }

  public boolean isInitialImport() {
    return !getLastVersion().isPresent();
  }

  public int getCurrentVersion() {
    return versionProvider.getCurrentVersion();
  }

  public Optional<Integer> getLastVersion() {
    return versionProvider.getLastVersion();
  }

  public void upgradeToCurrentVersion() {
    if (isInitialImport()) {
      initialImports.forEach(InitialImport::performInitialImport);
      versionProvider.writeLastVersion(getCurrentVersion());
    } else if (getLastVersion().get() < getCurrentVersion()) {
      List<VersionUpgrade> upgraders = new ArrayList<>(upgrades);
      Collections.sort(upgraders);

      upgraders.stream().filter(upgrader -> upgrader.getVersion() > getLastVersion().get()).forEach(upgrader -> {
        log.info("Performing upgrade from version {} to {} using {}", getLastVersion(), upgrader.getVersion(), upgrader.getClass().getSimpleName());
        upgrader.performUpgrade();
      });
      versionProvider.writeLastVersion(getCurrentVersion());
    } else {
      log.debug("no upgrade nessessary");
    }
  }

}
