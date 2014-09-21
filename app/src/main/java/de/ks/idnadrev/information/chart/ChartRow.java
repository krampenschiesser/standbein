/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.idnadrev.information.chart;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ChartRow {
  protected String category;

  protected Map<Integer, String> values = new HashMap<>();

  public String getValue(int columnId) {
    return values.get(columnId);
  }

  public void setValue(int columnId, String newValue) {
    values.put(columnId, newValue);
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public boolean isEmpty() {
    boolean empty = getCategory() == null || getCategory().isEmpty();
    Optional<Boolean> emptyValues = values.values().stream().map(s -> s == null || s.isEmpty()).reduce((o1, o2) -> o1 && o2);
    if (emptyValues.isPresent()) {
      return empty && emptyValues.get();
    } else {
      return empty;
    }
  }
}