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

import com.google.inject.ConfigurationException;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import de.ks.i18n.Localized;
import de.ks.imagecache.Images;
import de.ks.javafx.FxCss;
import de.ks.launch.ApplicationService;
import de.ks.launch.Launcher;
import de.ks.standbein.GuiceSupport;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ApplicationStartup {
  private static final Logger log = LoggerFactory.getLogger(ApplicationStartup.class);
  private MainWindow mainWindow;

  public void start(Stage stage) {
    Thread.currentThread().setUncaughtExceptionHandler(GuiceSupport.get(FXApplicationExceptionHandler.class));
    try {
      log.info("Starting application " + getClass().getName());
      try {
        mainWindow = GuiceSupport.get(MainWindow.class);
        stage.setTitle(mainWindow.getApplicationTitle());
        stage.setScene(createScene(mainWindow));

        Image icon = Images.get("appicon.png");
        if (icon != null) {
          stage.getIcons().add(icon);
        }
        Pane pane = (Pane) mainWindow.getNode();
        if (pane instanceof BorderPane) {
          Navigator.registerWithExistingPane(stage, (BorderPane) pane);
        } else {
          Navigator.register(stage, pane);
        }
      } catch (ConfigurationException e) {
        stage.setTitle(Localized.get("warning.general"));
        StackPane container = new StackPane();
        Label label = new Label(Localized.get("warning.unsatisfiedApplication"));
        container.getChildren().add(label);
        Scene scene = new Scene(container, 640, 480);
        stage.setScene(scene);

        Navigator.register(stage, container);
      }
      stage.setOnCloseRequest((WindowEvent e) -> {
        Launcher.instance.stopAll();
//        Launcher.instance.awaitStop();
      });
      Launcher.instance.getService(ApplicationService.class).setStage(stage);
      stage.show();
    } catch (Exception e) {
      log.error("Could not start JavaFXApp", e);
      throw e;
    }
  }

  private Scene createScene(MainWindow mainWindow) {
    Scene scene = new Scene(mainWindow.getNode());

    Key<Set<String>> key = Key.get(new TypeLiteral<Set<String>>() {
    }, FxCss.class);

    Set<String> styleSheets = GuiceSupport.instance.getInjector().getBinding(key).getProvider().get();
    styleSheets.forEach((sheet) -> {
      scene.getStylesheets().add(sheet);
    });
    return scene;
  }
}
