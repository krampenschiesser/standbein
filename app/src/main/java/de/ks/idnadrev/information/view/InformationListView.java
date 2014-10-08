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
package de.ks.idnadrev.information.view;

import de.ks.BaseController;
import de.ks.executor.group.LastTextChange;
import de.ks.fxcontrols.cell.ConvertingListCell;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.information.*;
import de.ks.idnadrev.tag.TagContainer;
import de.ks.reflection.PropertyPath;
import de.ks.validation.validators.NamedEntityValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class InformationListView extends BaseController<List<InformationPreviewItem>> {
  public static final int CELL_SIZE = 30;

  @FXML
  protected TextField nameSearch;
  @FXML
  protected TagContainer tagContainerController;
  @FXML
  protected ComboBox<Class<? extends Information<?>>> typeCombo;
  @FXML
  protected TableView<InformationPreviewItem> informationList;
  @FXML
  protected TableColumn<InformationPreviewItem, String> nameColumn;
  @FXML
  protected TableColumn<InformationPreviewItem, String> typeColumn;
  @FXML
  protected TableColumn<InformationPreviewItem, String> creationDateColumn;

  //  protected final SimpleIntegerProperty visibleItemCount = new SimpleIntegerProperty();
  protected final Map<String, Class<?>> comboValues = new HashMap<>();
  protected LastTextChange lastTextChange;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    lastTextChange = new LastTextChange(nameSearch, controller.getExecutorService());
    lastTextChange.registerHandler(cf -> triggerReload());

    typeCombo.setItems(FXCollections.observableArrayList(null, ChartInfo.class, FileInfo.class, HyperLinkInfo.class, TextInfo.class, UmlDiagramInfo.class));
    typeCombo.setCellFactory(l -> new ConvertingListCell<Class<? extends Information<?>>>(c -> Localized.get(c.getSimpleName())));
    typeCombo.getSelectionModel().select(0);
    typeCombo.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> triggerReload());

    nameColumn.setCellValueFactory(new PropertyValueFactory<>(PropertyPath.property(InformationPreviewItem.class, i -> i.getName())));
    typeColumn.setCellValueFactory(cd -> {
      InformationPreviewItem value = cd.getValue();
      String translation = Localized.get(value.getType().getSimpleName());
      return new SimpleStringProperty(translation);
    });
    creationDateColumn.setCellValueFactory(cd -> {
      LocalDate date = cd.getValue().getCreationTime().toLocalDate();
      String format = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).format(date);
      return new SimpleStringProperty(format);
    });

    validationRegistry.registerValidator(tagContainerController.getInput(), new NamedEntityValidator(Tag.class));
    tagContainerController.setReadOnly(true);
  }

  @Override
  protected void onRefresh(List<InformationPreviewItem> model) {
    super.onRefresh(model);

    informationList.setItems(FXCollections.observableList(model));
  }

  private void triggerReload() {
    store.getDatasource().setLoadingHint(createLoadingHint());
    controller.reload();
  }

  private InformationLoadingHint createLoadingHint() {
//    int itemsPerPage = visibleItemCount.get();
//    int firstResult = pager.getCurrentPageIndex() * itemsPerPage;
    int itemsPerPage = -1;
    int firstResult = -1;
    Class<? extends Information<?>> selectedItem = typeCombo.getSelectionModel().getSelectedItem();
    String name = nameSearch.textProperty().getValueSafe().toLowerCase(Locale.ROOT).trim();
    return new InformationLoadingHint(firstResult, itemsPerPage, selectedItem, name, null);
  }
}