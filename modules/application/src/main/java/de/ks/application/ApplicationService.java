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
package de.ks.application;

import com.google.inject.Injector;
import de.ks.activity.context.ActivityContext;
import de.ks.launch.Launcher;
import de.ks.launch.Service;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.*;

@Singleton
public class ApplicationService extends Service {
  private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

  //guice configuration dealing with FX singleton shit
  public static final String WAIT_FOR_INITIALIZATION = "waitForInitialization";
  public static final String PREVENT_PLATFORMEXIT = "preventPlatformExit";

  private static volatile Stage stage;
  public static volatile Injector singletonForFX;

  private String[] args;
  private final CountDownLatch latch = new CountDownLatch(1);
  private Future<?> fx;
  private boolean hasPreloader;
  private Launcher launcher;

  protected boolean waitForInitialization;
  protected boolean preventPlatformExit;

  final ActivityContext context;
  final ApplicationStartup startup;
  private final Injector injector;
  private final Navigator navigator;

  @Inject
  public ApplicationService(ActivityContext context, ApplicationStartup startup, Injector injector, Navigator navigator) {
    this.context = context;
    this.startup = startup;
    this.injector = injector;
    this.navigator = navigator;
  }

  @com.google.inject.Inject(optional = true)
  public void setWaitForInitialization(@Named(WAIT_FOR_INITIALIZATION) boolean wait) {
    this.waitForInitialization = wait;
  }

  @com.google.inject.Inject(optional = true)
  public void setPreventPlatformExit(@Named(PREVENT_PLATFORMEXIT) boolean preventExit) {
    this.preventPlatformExit = preventExit;
  }

  @Override
  public void initialize(Launcher launcher, ExecutorService executorService, String[] args) {
    super.initialize(launcher, executorService, args);
    this.args = args;
    this.launcher = launcher;
    hasPreloader = this.launcher.getPreloaderInstance() != null;
    if (hasPreloader) {
      stage = this.launcher.getPreloaderInstance().getStage();
    }
  }

  @Override
  protected void doStart() {
    log.info("Starting {}", getClass().getSimpleName());
    singletonForFX = injector;

    if (hasPreloader) {
      Platform.runLater(() -> startup.start(stage));
    } else if (stage == null) {
      fx = executorService.submit(() -> {
        try {
          Application.launch(App.class, args);
        } catch (Exception e) {
          log.error("Could not start application ", e);
        }
      });
    } else {
      navigator.register(stage, new StackPane());
      latch.countDown();
    }
    waitForJavaFXInitialized();
  }

  private void waitForJavaFXInitialized() {
    int timeout = 10;
    try {
      if (waitForInitialization) {
        latch.await();
      } else {
        boolean started = latch.await(timeout, TimeUnit.SECONDS);
        if (!started) {
          throw new RuntimeException("Could not start FX application.");
        }
      }
    } catch (InterruptedException e) {
      log.error("Got interrupted while waiting for FX application to start.", e);
    }
  }

  @Override
  protected void doStop() {
    context.stopAll();
    int timeout = 10;
    if (!preventPlatformExit) {
      Platform.exit();
    }
    try {
      latch.await(timeout, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      log.error("Got interrupted while waiting for FX application to stop.", e);
    }
  }

  public Stage getStage() {
    return stage;
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    latch.countDown();
    singletonForFX = null;
  }

  @Override
  public int getPriority() {
    return 5;
  }

  public void waitUntilFXFinished() throws ExecutionException, InterruptedException {
    if (hasPreloader) {
      launcher.waitForPreloader();
    } else {
      fx.get();
    }
  }
}
