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
import javafx.collections.ObservableList;
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

  private Stage stage;
  private StackPane rootContainer;
  private Node currentNode;

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

  public void present(Node node) {
    if (!node.equals(currentNode)) {
      rootContainer.getChildren().clear();
      rootContainer.getChildren().add(node);
      currentNode = node;
    }
  }

  public void register(Stage stage) {
    this.stage = stage;
    rootContainer = new StackPane();
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

  /**
   * Sets a new root container in the navigator and returns the old one.
   * If wanted the children of the old contaiber will be added to the new one.
   *
   * @param newRootContainer
   * @param addChildren      defines if the children shall be added again to the new container
   * @return the old root container
   */
  public StackPane setRootContainer(Pane newRootContainer, boolean addChildren) {
    StackPane oldRoot = this.rootContainer;
    ObservableList<Node> children = oldRoot.getChildren();
    stage.getScene().setRoot(newRootContainer);
    if (addChildren) {
      newRootContainer.getChildren().addAll(children);
    }
    return oldRoot;
  }

  private Scene createScene(StackPane rootContainer) {
    Scene scene = new Scene(rootContainer, applicationCfg.getWidth(), applicationCfg.getHeight());

    styleSheets.forEach((sheet) -> {
      scene.getStylesheets().add(sheet);
    });
    return scene;
  }

}
