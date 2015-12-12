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
package de.ks.version;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

public class VersionTest {
  private static final Logger log = LoggerFactory.getLogger(VersionTest.class);
  public static AtomicLong upgradeCounter = new AtomicLong();

  @Test
  public void testUpgrade() throws Exception {
    upgradeCounter.set(0);

    VersionProvider versionProvider = Mockito.mock(VersionProvider.class);
    Mockito.when(versionProvider.getLastVersion()).thenReturn(Optional.of(1));
    Mockito.when(versionProvider.getCurrentVersion()).thenReturn(3);

    Collection<InitialImport> imports = Collections.emptyList();
    Collection<VersionUpgrade> upgrades = Arrays.asList(new UpgraderVersion1(), new UpgraderVersion2(), new UpgraderVersion3());

    Versioning versioning = new Versioning(imports, upgrades, versionProvider);
    versioning.upgradeToCurrentVersion();
    assertEquals(2, upgradeCounter.get());
  }
}
