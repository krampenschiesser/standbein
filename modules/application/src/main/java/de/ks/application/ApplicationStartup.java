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

import de.ks.i18n.Localized;
import de.ks.imagecache.Images;
import de.ks.javafx.FxCss;
import de.ks.launch.ApplicationService;
import de.ks.launch.Launcher;
import de.ks.standbein.GuiceSupport;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

public class ApplicationStartup {
  private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);

  private MainWindow mainWindow;
  private final Navigator navigator;
  private final Localized localized;
  private final Launcher launcher;
  private final Set<String> styleSheets;

  @Inject
  public ApplicationStartup(Navigator navigator, Localized localized, Launcher launcher, @FxCss Set<String> styleSheets) {
    this.navigator = navigator;
    this.localized = localized;
    this.launcher = launcher;
    this.styleSheets = styleSheets == null ? Collections.emptySet() : styleSheets;
  }

  @com.google.inject.Inject(optional = true)
  public void setMainWindow(MainWindow mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void start(Stage stage) {
    try {
      Thread.currentThread().setUncaughtExceptionHandler(GuiceSupport.get(FXApplicationExceptionHandler.class));
      log.info("Starting application " + getClass().getName());
      if (mainWindow == null) {
        setWarning(stage);
      } else {
        setMainWindow(stage);
      }

      stage.setOnCloseRequest((WindowEvent e) -> {
        launcher.stopAll();
      });
      launcher.getService(ApplicationService.class).setStage(stage);
      stage.show();
    } catch (Exception e) {
      log.error("Could not start JavaFXApp", e);
      throw e;
    }
  }

  private void setMainWindow(Stage stage) {
    stage.setTitle(mainWindow.getApplicationTitle());
    stage.setScene(createScene(mainWindow));

    Image icon = Images.get("appicon.png");
    if (icon != null) {
      stage.getIcons().add(icon);
    }
    Pane pane = (Pane) mainWindow.getNode();
    navigator.register(stage, pane);
  }

  private void setWarning(Stage stage) {
    stage.setTitle(localized.get("warning.general"));

    StackPane container = new StackPane();
    Label label = new Label(localized.get("warning.unsatisfiedApplication"));
    container.getChildren().add(label);
    Scene scene = new Scene(container, 640, 480);
    stage.setScene(scene);

    navigator.register(stage, container);
  }

  private Scene createScene(MainWindow mainWindow) {
    Scene scene = new Scene(mainWindow.getNode());

    styleSheets.forEach((sheet) -> {
      scene.getStylesheets().add(sheet);
    });
    return scene;
  }
}
