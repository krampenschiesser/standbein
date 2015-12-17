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
package de.ks.standbein.menu;

import com.google.inject.Injector;
import de.ks.standbein.NodeProvider;
import de.ks.standbein.application.Navigator;
import javafx.scene.Node;

import java.util.function.Consumer;

public class ShowNodeAction implements Consumer<Injector> {
  protected final Class<? extends NodeProvider<? extends Node>> nodeProvider;
  protected boolean refreshOnReturn = true;
  protected String returnToActivity;

  public ShowNodeAction(Class<? extends NodeProvider<? extends Node>> nodeProvider) {
    this.nodeProvider = nodeProvider;
  }

  @Override
  public void accept(Injector injector) {
    Navigator navigator = injector.getInstance(Navigator.class);
    NodeProvider<? extends Node> nodeProviderInstance = injector.getInstance(nodeProvider);
    navigator.present(nodeProviderInstance.getNode());
  }
}
