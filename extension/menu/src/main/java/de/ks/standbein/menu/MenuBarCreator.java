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
import de.ks.standbein.i18n.Localized;
import de.ks.standbein.imagecache.Images;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class MenuBarCreator extends AbstractMenuCreator<MenuBar, MenuBarCreator> {
  private final Localized localized;
  private final ExecutorService executorService;
  private final Injector injector;
  private final Images images;

  @Inject
  public MenuBarCreator(Set<MenuEntry> items, Localized localized, ExecutorService executorService, Injector injector, Images images) {
    super(items);
    this.localized = localized;
    this.executorService = executorService;
    this.injector = injector;
    this.images = images;
  }

  @Override
  protected MenuBar createMenu(TreeSet<MenuEntry> menuEntries) {
    final Map<String, Menu> menus = new TreeMap<>();
    final List<String> menuNames = new ArrayList<>();
    final Map<MenuItem, MenuEntry> item2Entry = new HashMap<>();

    for (MenuEntry entry : menuEntries) {
      String currentItemMenuPath = entry.getPath();
      if (!menuNames.contains(currentItemMenuPath)) {
        menuNames.add(currentItemMenuPath);
      }
      Menu menu = getOrCreateMenu(currentItemMenuPath, menus);

      MenuItem menuItem = createMenuItem(entry);
      menu.getItems().add(menuItem);
      item2Entry.put(menuItem, entry);
    }

    List<Menu> rootMenus = createMenuTreeStructure(menuNames, menus);
    Map<Menu, Integer> menuOrder = new HashMap<>();

    for (Menu menu : rootMenus) {
      int minOrder = Integer.MAX_VALUE;
      ObservableList<MenuItem> items = menu.getItems();
      for (MenuItem item : items) {
        MenuEntry entry = item2Entry.get(item);
        if (entry != null) {
          int order = entry.getOrder();
          minOrder = Math.min(order, minOrder);
        }
      }
      menuOrder.put(menu, minOrder);
    }

    Collections.sort(rootMenus, Comparator.comparing(menuOrder::get));

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().addAll(rootMenus);
    return menuBar;
  }

  private MenuItem createMenuItem(MenuEntry entry) {
    MenuItem menuItem = new MenuItem();
    if (entry.getIconPath() != null && !entry.getIconPath().isEmpty()) {
      images.later(entry.getIconPath(), executorService)//
        .thenAccept(img -> menuItem.setGraphic(new ImageView(img)));
    }
    menuItem.setId(entry.getIconPath());
    if (entry.isLocalized()) {
      menuItem.setText(localized.get(entry.getName()));
    } else {
      menuItem.setText(entry.getName());
    }
    menuItem.setOnAction(e -> entry.getAction().accept(injector));
    return menuItem;
  }

  private Menu getOrCreateMenu(String currentItemMenuPath, Map<String, Menu> menus) {
    if (!menus.containsKey(currentItemMenuPath)) {
      createMenu(currentItemMenuPath, menus);
    }
    return menus.get(currentItemMenuPath);
  }

  private void createMenu(String menuPath, Map<String, Menu> menus) {
    Menu menu = new Menu();
    menu.setId(menuPath);
    menu.setText(localized.get(menuPath.toLowerCase(Locale.ROOT).substring(1).replace("/", ".")));
    menus.put(menuPath, menu);
  }

  private List<Menu> createMenuTreeStructure(List<String> menuNames, Map<String, Menu> menus) {
    List<String> rootMenuPaths = new ArrayList<>();

    for (String menuName : menuNames) {
      String seperator = AbstractMenuCreator.PATH_SEPARATOR;
      String[] menuPath = menuName.substring(1).split(seperator);
      String currentMenuName = seperator + menuPath[0];
      Menu lastMenu = null;
      for (int i = 1; i < menuPath.length; i++) {
        currentMenuName += seperator + menuPath[i];
        Menu currentMenu = getOrCreateMenu(currentMenuName, menus);
        if (lastMenu != null && !lastMenu.getItems().contains(currentMenu)) {
          lastMenu.getItems().add(currentMenu);
        }
        if (i == 1) {
          String rootName = seperator + menuPath[0] + seperator + menuPath[1];
          if (!rootMenuPaths.contains(rootName)) {
            rootMenuPaths.add(rootName);
          }
        }
        lastMenu = currentMenu;
      }
    }
    List<Menu> retval = rootMenuPaths.stream().map(path -> getOrCreateMenu(path, menus)).collect(Collectors.toList());
    return retval;
  }
}
