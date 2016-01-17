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
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TextFieldTableSelection<E> {
  private static final Logger log = LoggerFactory.getLogger(TextFieldTableSelection.class);

  final Localized localized;
  final ActivityExecutor executor;
  final ActivityJavaFXExecutor javaFXExecutor;
  GridPane root;
  Button browse;
  TextField textField;
  TableView<E> table;
  Function<String, List<String>> comboValueSupplier;
  Function<String, List<E>> tableItemSupplier;
  Function<TableView<E>, Pane> popupContentProvider = this::createPopupContent;
  LastTextChange lastTextChange;
  ListView<String> listView;
  PopupControl listPopup;
  PopupWindow tablePopup;
  Pane tablePopupContent;

  private SimpleObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();
  private SimpleObjectProperty<E> item = new SimpleObjectProperty<>();

  @Inject
  public TextFieldTableSelection(Localized localized, ActivityExecutor executor, ActivityJavaFXExecutor javaFXExecutor) {
    this.localized = localized;
    this.executor = executor;
    this.javaFXExecutor = javaFXExecutor;
  }

  public void configure(TableView<E> table, Function<String, List<String>> comboValueSupplier, Function<String, List<E>> tableItemSupplier, StringConverter<E> tableItemConverter) {
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
        if (getOnAction() != null) {
          getOnAction().handle(new ActionEvent());
        }
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
      selectListItem(listView.getSelectionModel(), tableItemConverter);
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
        if (listPopup.isShowing()) {
          selectListItem(selectionModel, tableItemConverter);
          e.consume();
        } else {
          if (onAction.get() != null) {
            onAction.get().handle(new ActionEvent());
          }
          e.consume();
        }
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

  void selectListItem(MultipleSelectionModel<String> selectionModel, StringConverter<E> tableItemConverter) {
    String selectedItem = selectionModel.getSelectedItem();
    if (selectedItem != null) {
      textField.setText(selectedItem);
      item.set(tableItemConverter.fromString(selectedItem));
      listPopup.hide();
      EventHandler<ActionEvent> handler = onAction.get();
      if (handler != null) {
        handler.handle(new ActionEvent());
      }
    }
  }

  void showTablePopup() {
    Bounds bounds = browse.localToScreen(browse.getLayoutBounds());
    double width = tablePopupContent.getWidth();
    tablePopup.show(browse, bounds.getMaxX() - width, bounds.getMinY());
  }

  void applyTableItem(TableView<E> table, StringConverter<E> tableItemConverter) {
    E selectedItem = table.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      String textValue = tableItemConverter.toString(selectedItem);
      textField.setText(textValue);
      this.item.set(selectedItem);
    }
  }

  void showListPopup() {
    Bounds bounds = textField.localToScreen(textField.getLayoutBounds());
    listPopup.show(textField, bounds.getMinX(), bounds.getMaxY());
    int selectedIndex = listView.getSelectionModel().getSelectedIndex();
    if (selectedIndex >= 0) {
      listView.scrollTo(selectedIndex);
    }
  }

  void setListItems(List<String> values) {
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
    checkConfigured();
    return textField;
  }

  public GridPane getRoot() {
    checkConfigured();
    return root;
  }

  public Button getBrowse() {
    checkConfigured();
    return browse;
  }

  private void checkConfigured() {
    if (root == null) {
      throw new IllegalStateException("Not configured yet!");
    }
  }

  public EventHandler<ActionEvent> getOnAction() {
    return onAction.get();
  }

  public SimpleObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
    return onAction;
  }

  public void setOnAction(EventHandler<ActionEvent> onAction) {
    this.onAction.set(onAction);
  }

  public E getItem() {
    return item.get();
  }

  public SimpleObjectProperty<E> itemProperty() {
    return item;
  }

  public void setItem(E item) {
    this.item.set(item);
  }
}
