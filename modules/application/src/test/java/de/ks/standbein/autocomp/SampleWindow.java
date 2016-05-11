/**
 * Copyright [2015] [Christian Loehnert]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.standbein.autocomp;

import de.ks.standbein.application.MainWindow;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class SampleWindow extends MainWindow {
  private static final Logger log = LoggerFactory.getLogger(SampleWindow.class);
  @Inject
  AutoCompletionTextField selection;

  @Override
  public Parent getNode() {

    Function<String, List<String>> tableItemSupplier = this::getTableItems;
    selection.configure(tableItemSupplier);
    selection.itemProperty().addListener((p, o, n) -> {
      log.info("Got new item {}", n);
    });
    return selection.getTextField();
  }

  private List<String> getTableItems(String value) {
    ArrayList<String> items = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      String name = "Item" + String.format("%03d", i);
      if (name.toLowerCase(Locale.ROOT).startsWith(value.toLowerCase(Locale.ROOT))) {
        items.add(name);
      }
    }
    return items;
  }

}
