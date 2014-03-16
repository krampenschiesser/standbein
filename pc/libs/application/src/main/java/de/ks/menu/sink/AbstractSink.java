package de.ks.menu.sink;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import com.google.common.eventbus.Subscribe;
import de.ks.activity.ActivityController;
import de.ks.eventsystem.bus.EventBus;
import de.ks.eventsystem.bus.HandlingThread;
import de.ks.eventsystem.bus.Threading;
import de.ks.menu.MenuItem;
import de.ks.menu.MenuItemDescriptor;
import de.ks.menu.event.MenuItemClickedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 *
 */
public abstract class AbstractSink<T extends AbstractSink> {
  protected final ActivityController controller;
  protected final EventBus bus;
  protected String menuPath;

  @Inject
  @MenuItem("")
  Instance<Object> menuItem;

  @Inject
  public AbstractSink(EventBus bus, ActivityController controller) {
    this.bus = bus;
    this.controller = controller;
  }

  @SuppressWarnings("unchecked")
  public T setMenuPath(String path) {
    this.menuPath = path;
    return (T) this;
  }

  protected EventBus getBus() {
    return bus;
  }

  public String getMenuPath() {
    return menuPath;
  }

  @PostConstruct
  public void register() {
    bus.register(this);
  }

  @PreDestroy
  public void deregister() {
    bus.unregister(this);
  }


  @Subscribe
  @Threading(HandlingThread.JavaFX)
  public boolean onEvent(MenuItemClickedEvent event) {
    if (getMenuPath() != null) {
      if (event.getItem().getMenuPath().startsWith(getMenuPath())) {
        Instance<?> select = menuItem.select(event.getTarget());
        Object menuItem = select.get();

        showMenuItem(menuItem, event.getItem());
        return true;
      }
    }
    return false;
  }

  protected abstract void showMenuItem(Object menuItem, MenuItemDescriptor item);
}