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
import de.ks.standbein.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.activity.DummyActivity;
import de.ks.standbein.application.Navigator;
import de.ks.util.FXPlatform;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class MenuTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new TestMenuModule(), new IntegrationTestModule()).launchServices();

  @Inject
  MenuBarCreator menu;
  @Inject
  Injector injector;
  @Inject
  ActivityController controller;
  @Inject
  Navigator navigator;
  @Inject
  TestNodeProvider testNodeProvider;

  @Test
  public void testMenu() throws Exception {
    MenuBar menu = this.menu.createMenu("/main/bla");
    ObservableList<Menu> sub = menu.getMenus();
    assertEquals(1, sub.size());

    Menu bla = sub.get(0);
    assertEquals(3, bla.getItems().size());//item 1, item 2, submenu
    assertEquals(TestMenuModule.BLA_ITEM_1, bla.getItems().get(0).getText());
    assertEquals(TestMenuModule.BLA_ITEM_2, bla.getItems().get(1).getText());
    assertEquals("?main.bla.blubb?", bla.getItems().get(2).getText());


    Menu blubb = (Menu) bla.getItems().stream().filter(item -> item instanceof Menu).findFirst().get();
    assertEquals(3, blubb.getItems().size());//item 1, item2, item3
    assertEquals(TestMenuModule.BLUBB_ITEM_2, blubb.getItems().get(0).getText());
    assertEquals(TestMenuModule.BLUBB_ITEM_1, blubb.getItems().get(1).getText());
    assertEquals(TestMenuModule.BLUBB_ITEM_3, blubb.getItems().get(2).getText());
  }

  @Test
  public void testStartActivity() throws Exception {
    MenuEntry entry = this.menu.items.stream().filter(i -> i.getName().equals(TestMenuModule.BLA_ITEM_1)).findFirst().get();
    FXPlatform.invokeLater(() -> entry.getAction().accept(injector));
    controller.waitForTasks();

    assertEquals(DummyActivity.class.getSimpleName(), controller.getCurrentActivityId());
  }

  @Test
  public void testShowNode() throws Exception {
    MenuEntry entry = this.menu.items.stream().filter(i -> i.getName().equals(TestMenuModule.SHOW_NODE)).findFirst().get();
    FXPlatform.invokeLater(() -> entry.getAction().accept(injector));
    FXPlatform.waitForFX();

    assertEquals(testNodeProvider.getNode(), navigator.getCurrentNode());

  }
}
