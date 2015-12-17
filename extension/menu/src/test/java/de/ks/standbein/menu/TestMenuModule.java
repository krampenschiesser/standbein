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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import de.ks.standbein.DummyActivity;

public class TestMenuModule extends AbstractModule {

  public static final String BLA_ITEM_1 = "TestItem A1";
  public static final String BLA_ITEM_2 = "TestItem A2";
  public static final String BLUBB_ITEM_1 = "TestItem B1";
  public static final String BLUBB_ITEM_2 = "TestItem B2";
  public static final String BLUBB_ITEM_3 = "TestItem B3";
  public static final String SHOW_NODE = "showNode";

  @Override
  protected void configure() {
    Multibinder<MenuEntry> itemBinder = Multibinder.newSetBinder(binder(), MenuEntry.class);
    itemBinder.addBinding().toInstance(new MenuEntry("/main/bla", BLA_ITEM_1, new StartActivityAction(DummyActivity.class)).setLocalized(false));
    itemBinder.addBinding().toInstance(new MenuEntry("/main/bla", BLA_ITEM_2, new StartActivityAction(DummyActivity.class)).setLocalized(false));
    itemBinder.addBinding().toInstance(new MenuEntry("/main/bla/blubb", BLUBB_ITEM_1, new StartActivityAction(DummyActivity.class)).setLocalized(false).setOrder(2));
    itemBinder.addBinding().toInstance(new MenuEntry("/main/bla/blubb", BLUBB_ITEM_2, new StartActivityAction(DummyActivity.class)).setLocalized(false).setOrder(1));
    itemBinder.addBinding().toInstance(new MenuEntry("/main/bla/blubb", BLUBB_ITEM_3, new StartActivityAction(DummyActivity.class)).setLocalized(false).setOrder(2));

    itemBinder.addBinding().toInstance(new MenuEntry("/urks", SHOW_NODE, new ShowNodeAction(TestNodeProvider.class)));
  }
}
