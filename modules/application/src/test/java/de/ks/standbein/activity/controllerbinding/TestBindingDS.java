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
package de.ks.standbein.activity.controllerbinding;

import de.ks.standbein.datasource.DataSource;

import java.util.function.Consumer;

public class TestBindingDS implements DataSource<Option> {

  private Option saved = new Option("test").setValue(42);

  public Option getSaved() {
    return saved;
  }

  @Override
  public Option loadModel(Consumer<Option> furtherProcessing) {
    furtherProcessing.accept(saved);
    return saved;
  }

  @Override
  public void saveModel(Option model, Consumer<Option> beforeSaving) {
    beforeSaving.accept(model);
    this.saved = model;
  }
}
