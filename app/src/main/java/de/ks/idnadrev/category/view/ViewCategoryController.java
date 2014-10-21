/**
 * Copyright [2014] [Christian Loehnert]
 *
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
package de.ks.idnadrev.category.view;

import de.ks.BaseController;
import de.ks.idnadrev.category.CategoryBrowser;
import de.ks.idnadrev.entity.Category;
import javafx.fxml.FXML;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewCategoryController extends BaseController<List<Category>> {
  @FXML
  protected CategoryBrowser categoryBrowserController;

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }

  @Override
  protected void onRefresh(List<Category> model) {
    super.onRefresh(model);
  }
}
