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

import java.util.function.Consumer;

public class MenuEntry implements Comparable<MenuEntry> {
  protected final String name;
  protected final String path;
  protected final Consumer<Injector> action;
  protected String iconPath;
  protected int order;
  protected boolean localized;

  public MenuEntry(String path, String name, Consumer<Injector> action) {
    this.name = name;
    this.path = path;
    this.action = action;
  }

  public MenuEntry setIconPath(String iconPath) {
    this.iconPath = iconPath;
    return this;
  }

  public MenuEntry setOrder(int order) {
    this.order = order;
    return this;
  }

  public MenuEntry setLocalized(boolean localized) {
    this.localized = localized;
    return this;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public Consumer<Injector> getAction() {
    return action;
  }

  public String getIconPath() {
    return iconPath;
  }

  public boolean isLocalized() {
    return localized;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MenuEntry)) {
      return false;
    }

    MenuEntry menuEntry = (MenuEntry) o;
    if (!name.equals(menuEntry.name)) {
      return false;
    }
    return path.equals(menuEntry.path);

  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + path.hashCode();
    return result;
  }

  @Override
  public int compareTo(MenuEntry o) {
    int comparison = path.compareTo(o.getPath());
    if (comparison == 0) {
      comparison = Integer.compare(order, o.order);
      if (comparison == 0) {
        return name.compareTo(o.name);
      }
    }
    return comparison;
  }
}
