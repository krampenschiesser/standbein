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
package de.ks.standbein.table.sample;

import de.ks.standbein.application.MainWindow;
import de.ks.standbein.table.MyTableItem;
import de.ks.standbein.table.TableConfigurator;
import de.ks.standbein.table.selection.TextFieldTableSelection;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SampleWindow extends MainWindow {
  private static final Logger log = LoggerFactory.getLogger(SampleWindow.class);
  @Inject
  TextFieldTableSelection<MyTableItem> selection;
  @Inject
  TableConfigurator<MyTableItem> configurator;

  @Override
  public Parent getNode() {
    TableView<MyTableItem> tableView = new TableView<>();
    configurator.addText(MyTableItem.class, MyTableItem::getName).setWidth(75).setName("Name");
    configurator.addDate(MyTableItem.class, MyTableItem::getDate).setWidth(100).setName("Date");
    configurator.addDateTime(MyTableItem.class, MyTableItem::getTime).setWidth(150).setName("Date/Time");
    configurator.addNumber(MyTableItem.class, MyTableItem::getCount).setName("Count");
    configurator.configureTable(tableView);

    Function<String, List<MyTableItem>> tableItemSupplier = this::getTableItems;
    StringConverter<MyTableItem> tableItemConverter = new StringConverter<MyTableItem>() {
      @Override
      public String toString(MyTableItem object) {
        return object.getName();
      }

      @Override
      public MyTableItem fromString(String string) {
        return tableItemSupplier.apply(string).get(0);
      }
    };
    selection.configure(tableView, this::getComboValue, tableItemSupplier, tableItemConverter);
    selection.getBrowse().setText("Browse");
    selection.itemProperty().addListener((p, o, n) -> {
      log.info("Got new item {}", n);
    });
    return selection.getRoot();
  }

  private List<MyTableItem> getTableItems(String value) {
    ArrayList<MyTableItem> items = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      MyTableItem item = new MyTableItem().setName("Item" + String.format("%03d", i)).setTime(LocalDateTime.now().minusHours(i)).setCount((short) i).setDate(LocalDateTime.now().minusHours(i).toLocalDate());
      if (item.getName().startsWith(value.trim())) {
        items.add(item);
      }
    }
    return items;
  }

  private List<String> getComboValue(String s) {
    return getTableItems(s).stream().filter(i -> i.getName().startsWith(s)).map(MyTableItem::getName).collect(Collectors.toList());
  }
}
