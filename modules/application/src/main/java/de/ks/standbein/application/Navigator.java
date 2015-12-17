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
package de.ks.standbein.application;

import de.ks.standbein.i18n.Localized;
import de.ks.standbein.imagecache.Images;
import de.ks.standbein.javafx.FxCss;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class Navigator {
  private Set<String> styleSheets = new HashSet<>();
  private ApplicationCfg applicationCfg = new ApplicationCfg("Unknown", 800, 600);
  private final Localized localized;

  Stage stage;
  Pane rootContainer;
  StackPane contentContainer;
  Node currentNode;

  @Inject
  public Navigator(Localized localized) {
    this.localized = localized;
  }

  @com.google.inject.Inject(optional = true)
  public void setStyleSheets(@FxCss Set<String> sheets) {
    this.styleSheets = sheets;
  }

  @com.google.inject.Inject(optional = true)
  public void setApplicationCfg(ApplicationCfg cfg) {
    this.applicationCfg = cfg;
  }

  public Node getCurrentNode() {
    return currentNode;
  }

  public void changeRootContainer(Pane root, StackPane content) {
    this.rootContainer = root;
    stage.getScene().setRoot(root);
    this.contentContainer = content;
  }

  public void present(Node node) {
    if (node != null && !node.equals(currentNode)) {
      contentContainer.getChildren().clear();
      contentContainer.getChildren().add(node);
      currentNode = node;
    }
  }

  public void register(Stage stage) {
    this.stage = stage;
    contentContainer = new StackPane();
    rootContainer = contentContainer;
    Scene scene = createScene(rootContainer);
    stage.setScene(scene);
    String title = applicationCfg.getTitle();
    if (applicationCfg.isLocalized()) {
      title = localized.get(title);
    }
    stage.setTitle(title);

    if (applicationCfg.getIcon() != null) {
      Image icon = Images.get(applicationCfg.getIcon());
      if (icon != null) {
        stage.getIcons().add(icon);
      }
    }
    stage.getScene().setRoot(rootContainer);
  }

  private Scene createScene(Pane rootContainer) {
    Scene scene = new Scene(rootContainer, applicationCfg.getWidth(), applicationCfg.getHeight());

    styleSheets.forEach((sheet) -> {
      scene.getStylesheets().add(sheet);
    });
    return scene;
  }

}
