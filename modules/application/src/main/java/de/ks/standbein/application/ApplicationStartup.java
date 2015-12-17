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
package de.ks.standbein.application;

import com.google.inject.Injector;
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.activity.ActivityHint;
import de.ks.standbein.activity.InitialActivity;
import de.ks.standbein.i18n.Localized;
import de.ks.standbein.launch.Launcher;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class ApplicationStartup {
  private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);

  private MainWindow mainWindow;
  private InitialActivity initalActivity;
  private final Navigator navigator;
  private final Localized localized;
  private final Injector injector;
  private final Provider<Launcher> launcher;
  private FXApplicationExceptionHandler exceptionHandler;

  @Inject
  public ApplicationStartup(Navigator navigator, Localized localized, Injector injector, Provider<Launcher> launcher) {
    this.navigator = navigator;
    this.localized = localized;
    this.injector = injector;
    this.launcher = launcher;
  }

  @com.google.inject.Inject(optional = true)
  public void setInitialActivity(InitialActivity initial) {
    this.initalActivity = initial;
  }

  @com.google.inject.Inject(optional = true)
  public void setMainWindow(MainWindow window) {
    this.mainWindow = window;
  }

  @com.google.inject.Inject(optional = true)
  public void setExceptionHandler(FXApplicationExceptionHandler handler) {
    this.exceptionHandler = handler;
  }

  public void start(Stage stage) {
    try {
      Thread.currentThread().setUncaughtExceptionHandler(exceptionHandler);
      log.info("Starting application " + getClass().getName());
      navigator.register(stage);

      if (mainWindow != null) {
        ApplicationRoot root = mainWindow.getRoot();
        navigator.changeRootContainer(root.getRoot(), root.getPresentationArea());
        navigator.present(mainWindow.getNode());
      }
      if (mainWindow == null && initalActivity == null) {
        setWarning(stage);
      }

      stage.setOnCloseRequest((WindowEvent e) -> {
        launcher.get().stopAll();
      });
      launcher.get().getService(ApplicationService.class).setStage(stage);

      if (initalActivity != null) {
        Platform.runLater(() -> injector.getInstance(ActivityController.class).startOrResume(new ActivityHint(initalActivity.getInitialActivity())));
//        Platform.runLater(() -> controller.startOrResume(new ActivityHint(initalActivity)));
      }
      stage.show();
    } catch (Exception e) {
      log.error("Could not start JavaFXApp", e);
      throw e;
    }
  }

  private void setWarning(Stage stage) {
    stage.setTitle(localized.get("warning.general"));

    StackPane container = new StackPane();
    Label label = new Label(localized.get("warning.unsatisfiedApplication"));
    container.getChildren().add(label);

    navigator.register(stage);
    navigator.present(container);
  }
}
