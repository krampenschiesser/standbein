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

import javafx.scene.Node;

import javax.inject.Inject;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractMenuCreator<N extends Node, T extends AbstractMenuCreator<N, T>> {
  public static final String PATH_SEPARATOR = "/";
  final Set<MenuEntry> items;

  @Inject
  public AbstractMenuCreator(Set<MenuEntry> items) {
    this.items = new TreeSet<>(items);
  }

  public N createMenu(String prefix) {
    return createMenu(i -> i.getPath().startsWith(prefix));
  }

  public N createMenu(Predicate<MenuEntry> itemFilter) {
    return createMenu(new TreeSet<>(items.stream().filter(itemFilter).collect(Collectors.toList())));
  }

  protected abstract N createMenu(TreeSet<MenuEntry> menuEntries);
}
