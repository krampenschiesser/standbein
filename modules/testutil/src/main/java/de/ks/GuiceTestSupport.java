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
  protected boolean executeStop = true;

  public GuiceTestSupport(Object test, Module... modules) {
    this.test = test;
    injector = Guice.createInjector(modules);
  }

  @Override
  protected void starting(Description description) {
    if (launch) {
      log.info("###Starting services before {}.{}", description.getTestClass().getSimpleName(), description.getMethodName());
      Launcher launcher = injector.getInstance(Launcher.class);
      if (!launcher.isStarted()) {
        launcher.startAll();
        launcher.awaitStart();
      }
      log.info("###Starting services -> done");
    }
    injector.injectMembers(test);
  }

  @Override
  protected void finished(Description description) {
    if (launch && executeStop) {
      log.info("###Stopping services after {}.{}", description.getTestClass().getSimpleName(), description.getMethodName());
      Launcher launcher = injector.getInstance(Launcher.class);
      if (launcher.isStarted()) {
        launcher.stopAll();
        launcher.awaitStop();
      }
      log.info("###Stopping services -> done");
    } else {
      log.warn("###Prevent stop of launched services!");
    }
  }

  public GuiceTestSupport launchServices() {
    launch = true;
    return this;
  }

  public GuiceTestSupport preventServiceStop() {
    launch = true;
    executeStop = false;
    return this;
  }
}
