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

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.inject.Singleton;

@Singleton
public class Navigator {
  private Stage stage;
  private StackPane rootContainer;
  private Node currentNode;

  public void present(Node node) {
    rootContainer.getChildren().clear();
    rootContainer.getChildren().add(node);
  }

  public void register(Stage stage, Node rootItem) {
    this.stage = stage;
    rootContainer = new StackPane();
    currentNode = rootItem;
    present(rootItem);
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
}
