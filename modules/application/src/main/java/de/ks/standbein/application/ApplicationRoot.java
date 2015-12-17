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

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class ApplicationRoot {
  protected final StackPane presentationArea;
  protected final Pane root;

  public ApplicationRoot(Pane root, StackPane presentationArea) {
    this.presentationArea = presentationArea;
    this.root = root;
  }

  public StackPane getPresentationArea() {
    return presentationArea;
  }

  public Pane getRoot() {
    return root;
  }
}
