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
package de.ks;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import de.ks.launch.Launcher;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiceTestSupport extends TestWatcher {
  private static final Logger log = LoggerFactory.getLogger(GuiceTestSupport.class);
  private final Injector injector;
  private final Object test;
  protected boolean launch;

  public GuiceTestSupport(Object test, Module... modules) {
    this.test = test;
    injector = Guice.createInjector(modules);
  }

  protected void starting(Description description) {
    injector.injectMembers(test);

    if (launch) {
      Launcher launcher = injector.getInstance(Launcher.class);
      if (!launcher.isStarted()) {
        launcher.startAll();
        launcher.awaitStart();
      }
    }
  }

  public GuiceTestSupport launchServices() {
    launch = true;
    return this;
  }
}
