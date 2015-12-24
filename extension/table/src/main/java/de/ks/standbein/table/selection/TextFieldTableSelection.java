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
package de.ks.standbein.table.selection;

import de.ks.executor.group.LastTextChange;
import de.ks.standbein.activity.executor.ActivityExecutor;
import de.ks.standbein.activity.executor.ActivityJavaFXExecutor;
import de.ks.standbein.i18n.Localized;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.PopupWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TextFieldTableSelection<E> {
  private static final Logger log = LoggerFactory.getLogger(TextFieldTableSelection.class);

  private final Localized localized;
  private final ActivityExecutor executor;
  private final ActivityJavaFXExecutor javaFXExecutor;
  protected GridPane root;
  protected Button browse;
  protected TextField textField;
  private TableView<E> table;
  private Function<String, List<String>> comboValueSupplier;
  private Function<String, List<E>> tableItemSupplier;
  private Function<TableView<E>, Pane> popupContentProvider = this::createPopupContent;
  private LastTextChange lastTextChange;
  private ListView<String> listView;
  private PopupControl listPopup;
  private PopupWindow tablePopup;
  private Pane tablePopupContent;

  @Inject
  public TextFieldTableSelection(Localized localized, ActivityExecutor executor, ActivityJavaFXExecutor javaFXExecutor) {
    this.localized = localized;
    this.executor = executor;
    this.javaFXExecutor = javaFXExecutor;
  }

  public void configure(TableView<E> table, Function<String, List<String>> comboValueSupplier, Function<String, List<E>> tableItemSupplier, Function<E, String> tableItemConverter) {
    this.table = table;
    this.comboValueSupplier = comboValueSupplier;
    this.tableItemSupplier = tableItemSupplier;


    textField = new TextField();
    browse = new Button(localized.get(getClass().getSimpleName() + ".browse"));
    root = new GridPane();
    tablePopup = new PopupControl();
    listView = new ListView<>();
    listPopup = new PopupControl();
    lastTextChange = new LastTextChange(textField, 75, executor);

    tablePopupContent = popupContentProvider.apply(table);
    tablePopupContent.setOnKeyReleased(e -> {
      if (e.getCode() == KeyCode.ENTER) {
        tablePopup.hide();
        browse.requestFocus();
        applyTableItem(table, tableItemConverter);

      } else if (e.getCode() == KeyCode.ESCAPE) {
        tablePopup.hide();
        browse.requestFocus();
      }
    });
    tablePopup.getScene().setRoot(tablePopupContent);
    table.setOnMouseClicked(e -> {
      if (e.getClickCount() > 1) {
        applyTableItem(table, tableItemConverter);
        tablePopup.hide();
      }
    });
    tablePopup.setAutoHide(true);

    listView.setFocusTraversable(false);
    listView.setMaxHeight(Control.USE_PREF_SIZE);
    listView.setPrefHeight(Control.USE_COMPUTED_SIZE);
    listView.setMinHeight(Control.USE_PREF_SIZE);
    listView.fixedCellSizeProperty().bind(textField.heightProperty());
    listView.setEffect(new DropShadow());
    listView.setOnMouseClicked(e -> {
      String item = listView.getSelectionModel().getSelectedItem();
      if (item != null) {
        textField.setText(item);
        listPopup.hide();
      }
    });
    listPopup.getScene().setRoot(listView);

    root.setHgap(5D);

    root.getColumnConstraints().add(new ColumnConstraints(Control.USE_PREF_SIZE, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, HPos.LEFT, true));
    root.getColumnConstraints().add(new ColumnConstraints(Control.USE_PREF_SIZE, Control.USE_COMPUTED_SIZE, Control.USE_PREF_SIZE, Priority.NEVER, HPos.LEFT, true));
    root.getRowConstraints().add(new RowConstraints(30, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.NEVER, VPos.BASELINE, true));

    root.add(textField, 0, 0);
    root.add(browse, 1, 0);


    lastTextChange.registerHandler(cf -> cf.thenApply(comboValueSupplier::apply).thenAcceptAsync(values -> setListItems(values), javaFXExecutor));

    textField.setOnKeyPressed(e -> {
      MultipleSelectionModel<String> selectionModel = listView.getSelectionModel();

      if (e.getCode() == KeyCode.DOWN) {
        int idx = selectionModel.getSelectedIndex() + 1;
        idx = idx >= listView.getItems().size() ? 0 : idx;
        selectionModel.select(idx);
        listView.scrollTo(Math.max(idx - 2, 0));
      } else if (e.getCode() == KeyCode.UP) {
        int idx = selectionModel.getSelectedIndex() - 1;
        idx = idx < 0 ? listView.getItems().size() - 1 : idx;
        selectionModel.select(idx);
        listView.scrollTo(Math.max(idx - 2, 0));
      } else if (e.getCode() == KeyCode.ENTER) {
        String selectedItem = selectionModel.getSelectedItem();
        textField.setText(selectedItem);
        listPopup.hide();
      } else if (e.getCode() == KeyCode.ESCAPE) {
        listPopup.hide();
      } else if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
        showListPopup();
      }
    });
    textField.focusedProperty().addListener((p, o, n) -> {
      if (n) {
        showListPopup();
      } else {
        listPopup.hide();
      }
    });

    browse.setOnAction(e -> {
      showTablePopup();
      CompletableFuture.supplyAsync(() -> tableItemSupplier.apply(textField.textProperty().getValueSafe()), executor)//
        .thenAcceptAsync(items -> table.setItems(FXCollections.observableArrayList(items)), javaFXExecutor);
    });
    tablePopup.setOnShown(e -> {
      showTablePopup();
    });
    CompletableFuture.supplyAsync(() -> comboValueSupplier.apply(textField.textProperty().getValueSafe()), executor)//
      .thenAcceptAsync(this::setListItems, javaFXExecutor);
  }

  private void showTablePopup() {
    Bounds bounds = browse.localToScreen(browse.getLayoutBounds());
    double width = tablePopupContent.getWidth();
    tablePopup.show(browse, bounds.getMaxX() - width, bounds.getMinY());
  }

  private void applyTableItem(TableView<E> table, Function<E, String> tableItemConverter) {
    E selectedItem = table.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      String textValue = tableItemConverter.apply(selectedItem);
      textField.setText(textValue);
    }
  }

  private void showListPopup() {
    Bounds bounds = textField.localToScreen(textField.getLayoutBounds());
    listPopup.show(textField, bounds.getMinX(), bounds.getMaxY());
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    if (selectedIndex >= 0) {
      listView.scrollTo(selectedIndex);
    }
  }

  private void setListItems(List<String> values) {
    listView.setItems(FXCollections.observableArrayList(values));
    double fixedCellSize = listView.getFixedCellSize();
    fixedCellSize = fixedCellSize <= 0 ? 25 : fixedCellSize;
    fixedCellSize += 1;
    double height = fixedCellSize * Math.min(5, values.size());
    listView.setPrefHeight(height);
  }

  private Pane createPopupContent(TableView<E> tableView) {
    StackPane stackPane = new StackPane();
    stackPane.getChildren().add(tableView);
    stackPane.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), null)));
    stackPane.setPadding(new Insets(5));
    stackPane.setEffect(new DropShadow());
    return stackPane;
  }

  public TextField getTextField() {
    return textField;
  }

  public GridPane getRoot() {
    return root;
  }

  public Button getBrowse() {
    return browse;
  }
}
