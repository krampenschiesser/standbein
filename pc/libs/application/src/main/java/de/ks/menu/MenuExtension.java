package de.ks.menu;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MenuExtension implements Extension {
  private static final Logger log = LoggerFactory.getLogger(MenuExtension.class);

  protected final TreeMap<String, MenuItemDescriptor> menuEntries = new TreeMap<>();

  public void onBean(@Observes ProcessBean event) {
    if (event.getAnnotated().isAnnotationPresent(MenuItem.class)) {
      MenuItem annotation = event.getAnnotated().getAnnotation(MenuItem.class);

      MenuItemDescriptor menuItemDescriptor = new MenuItemDescriptor(annotation, event.getBean().getBeanClass());
      menuEntries.put(menuItemDescriptor.getMenuItemPath(), menuItemDescriptor);
    }
    log.info("Discovered bean {}", event.getBean().getBeanClass().getName());
  }

  public TreeMap<String, MenuItemDescriptor> getMenuEntries() {
    return menuEntries;
  }

  public List<MenuItemDescriptor> getMenuEntries(String menuPath) {
    return menuEntries.values().parallelStream()//
            .filter((MenuItemDescriptor item) -> item.getMenuItemPath().startsWith(menuPath))//
            .sorted()//
            .collect(Collectors.toList());
  }

  public List<Class<?>> getMenuClasses() {
    return getMenuEntries().values().stream().map(item -> item.getTarget()).collect(Collectors.toList());
  }
}
