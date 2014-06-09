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
package de.ks.activity.listbinding;

import de.ks.activity.DetailItem;
import de.ks.datasource.ListDataSource;

import java.util.Arrays;
import java.util.List;

public class DummyDetailItemDataSource implements ListDataSource<DetailItem> {

  @Override
  public List<DetailItem> loadModel() {
    return Arrays.asList(new DetailItem().setName("Name1").setDescription("Desc1"), new DetailItem().setName("Name2").setDescription("Desc2"));
  }

  @Override
  public void saveModel(List<DetailItem> items) {
    //
  }
}