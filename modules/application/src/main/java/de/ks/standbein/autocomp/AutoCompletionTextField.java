/**
 * Copyright [2016] [Christian Loehnert]
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

import de.ks.executor.group.LastTextChange;
import de.ks.standbein.activity.executor.ActivityExecutor;
import de.ks.standbein.activity.executor.ActivityJavaFXExecutor;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AutoCompletionTextField {
  final ActivityExecutor executor;
  final ActivityJavaFXExecutor javaFXExecutor;
  private Function<String, List<String>> comboValueSupplier;
  private TextField textField;
  private ListView<String> listView;
  private PopupControl listPopup;
  private LastTextChange lastTextChange;

  private SimpleObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();
  private SimpleObjectProperty<String> item = new SimpleObjectProperty<>();

  @Inject
  public AutoCompletionTextField(ActivityExecutor executor, ActivityJavaFXExecutor javaFXExecutor) {
    this.executor = executor;
    this.javaFXExecutor = javaFXExecutor;
  }

  public void configure(Function<String, List<String>> comboValueSupplier) {
    configure(comboValueSupplier, null);
  }

  public void configure(Function<String, List<String>> comboValueSupplier, TextField externalTextField) {
    this.comboValueSupplier = comboValueSupplier;

    if (externalTextField == null) {
      textField = new TextField();
    } else {
      textField = externalTextField;
    }
    listView = new ListView<>();
    listPopup = new PopupControl();
    lastTextChange = new LastTextChange(textField, 75, executor);

    listView.setFocusTraversable(false);
    listView.setMaxHeight(Control.USE_PREF_SIZE);
    listView.setPrefHeight(Control.USE_COMPUTED_SIZE);
    listView.setMinHeight(Control.USE_PREF_SIZE);
    listView.fixedCellSizeProperty().bind(textField.heightProperty());
    listView.setEffect(new DropShadow());
    listView.setOnMouseClicked(e -> {
      selectListItem(listView.getSelectionModel());
    });
    listPopup.getScene().setRoot(listView);


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
          selectListItem(selectionModel);
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

    CompletableFuture.supplyAsync(() -> comboValueSupplier.apply(textField.textProperty().getValueSafe()), executor)//
      .thenAcceptAsync(this::setListItems, javaFXExecutor);
  }

  void selectListItem(MultipleSelectionModel<String> selectionModel) {
    String selectedItem = selectionModel.getSelectedItem();
    if (selectedItem != null) {
      textField.setText(selectedItem);
      item.set(selectedItem);
      listPopup.hide();
      EventHandler<ActionEvent> handler = onAction.get();
      if (handler != null) {
        handler.handle(new ActionEvent());
      }
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

  public String getItem() {
    return item.get();
  }

  public SimpleObjectProperty<String> itemProperty() {
    return item;
  }

  public void setItem(String item) {
    this.item.set(item);
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

  public TextField getTextField() {
    return textField;
  }
}
