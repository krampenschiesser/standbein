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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.ks.BaseController;
import de.ks.i18n.Localized;
import de.ks.idnadrev.entity.information.ChartInfo;
import de.ks.validation.ValidationMessage;
import de.ks.validation.validators.DoubleValidator;
import de.ks.validation.validators.NotEmptyValidator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ChartDataEditor extends BaseController<ChartInfo> {
  private static final Logger log = LoggerFactory.getLogger(ChartDataEditor.class);
  private static final int ROW_OFFSET = 1;
  private static final int COLUMN_OFFSET = 1;
  @FXML
  public TextField xaxisTitle;
  @FXML
  protected Button addColumn;
  @FXML
  protected GridPane dataContainer;
  @FXML
  protected GridPane root;

  protected final ObservableList<ChartRow> rows = FXCollections.observableArrayList();
  protected final ObservableList<SimpleStringProperty> columnHeaders = FXCollections.observableArrayList();

  protected final List<TextField> headers = new ArrayList<>();
  protected final List<TextField> categoryEditors = new ArrayList<>();
  protected final Table<Integer, Integer, TextField> valueEditors = HashBasedTable.create();
  protected Consumer<ChartPreviewData> callback;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    columnHeaders.addListener((ListChangeListener<SimpleStringProperty>) c -> onColumnsChanged(c));
    rows.addListener((ListChangeListener<ChartRow>) c -> onRowsChanged(c));
    rows.add(new ChartRow());
    columnHeaders.add(new SimpleStringProperty(Localized.get("col", 1)));
    columnHeaders.add(new SimpleStringProperty(Localized.get("col", 2)));

    validationRegistry.registerValidator(xaxisTitle, new NotEmptyValidator());
  }

  protected void onRowsChanged(ListChangeListener.Change<? extends ChartRow> c) {
    while (c.next()) {
      List<? extends ChartRow> addedSubList = c.getAddedSubList();

      for (ChartRow chartRow : addedSubList) {
        int rowNum = rows.indexOf(chartRow);

        TextField categoryEditor = createCategoryEditor(chartRow, rowNum);
        addRowConstraint();
        dataContainer.add(categoryEditor, 0, rowNum + ROW_OFFSET);

        for (int i = 0; i < columnHeaders.size(); i++) {
          TextField editor = createValueEditor(chartRow, rowNum, i);
          editor.setText(chartRow.getValue(i));
        }
      }
    }
  }

  private TextField createCategoryEditor(ChartRow chartRow, int rowNum) {
    TextField categoryEditor = new TextField();
    categoryEditor.setText(chartRow.getCategory());
    categoryEditor.focusedProperty().addListener((p, o, n) -> {
      if (n) {
        if (rowNum + ROW_OFFSET == rows.size()) {
          rows.add(new ChartRow());
        }
        categoryEditor.setUserData(false);
      } else if (o && !n) {
        boolean edited = (Boolean) (categoryEditor.getUserData() == null ? false : categoryEditor.getUserData());
        if (edited) {
          triggerRedraw();
          categoryEditor.setUserData(false);
        }
      }
    });
    categoryEditor.textProperty().addListener((p, o, n) -> {
      chartRow.setCategory(categoryEditor.getText());
      categoryEditor.setUserData(true);
    });
    categoryEditor.setOnKeyTyped(e -> {
      boolean selectNext = false;
      if (e.getCode() == KeyCode.UNDEFINED) {
        if (e.getCharacter().equals("\r")) {
          selectNext = true;
        }
      } else if (e.getCode() == KeyCode.ENTER) {
        selectNext = true;
      }
      if (selectNext) {
        int next = rowNum + ROW_OFFSET;
        if (categoryEditors.size() > next) {
          categoryEditors.get(next).requestFocus();
        }
        e.consume();
      }
    });
    validationRegistry.registerValidator(categoryEditor, (control, value) -> {
      if (value != null) {
        Set<String> values = categoryEditors.stream()//
                .filter(e -> e != categoryEditor)//
                .map(e -> e.textProperty().getValueSafe())//
                .filter(v -> !v.isEmpty())//
                .collect(Collectors.toSet());
        if (values.contains(value)) {
          ValidationMessage message = new ValidationMessage(Localized.get("validation.noDuplicates"), control, value);
          return ValidationResult.fromMessages(message);
        }
      }
      return null;
    });
    categoryEditors.add(categoryEditor);
    return categoryEditor;
  }

  protected void onColumnsChanged(ListChangeListener.Change<? extends SimpleStringProperty> c) {
    while (c.next()) {
      List<? extends SimpleStringProperty> added = c.getAddedSubList();

      for (SimpleStringProperty column : added) {
        int columnIndex = columnHeaders.indexOf(column);
        addColumnConstraint();

        TextField title = new TextField();
        title.textProperty().bindBidirectional(column);
        title.getStyleClass().add("editorViewLabel");
        headers.add(title);
        dataContainer.add(title, columnIndex + COLUMN_OFFSET, 0);

        for (int i = 0; i < rows.size(); i++) {
          ChartRow chartRow = rows.get(i);
          String value = chartRow.getValue(columnIndex);

          TextField editor = createValueEditor(chartRow, i, columnIndex);
          editor.setText(value);
        }
      }
    }
  }

  protected void addRowConstraint() {
    dataContainer.getRowConstraints().add(new RowConstraints(30, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.NEVER, VPos.CENTER, true));
  }

  protected void addColumnConstraint() {
    dataContainer.getColumnConstraints().add(new ColumnConstraints(Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Control.USE_COMPUTED_SIZE, Priority.SOMETIMES, HPos.CENTER, true));
  }

  protected TextField createValueEditor(ChartRow chartRow, int rowNum, int column) {
    TextField editor = new TextField();
    valueEditors.put(rowNum, column, editor);
    validationRegistry.registerValidator(editor, new DoubleValidator());
    dataContainer.add(editor, column + COLUMN_OFFSET, rowNum + ROW_OFFSET);

    editor.focusedProperty().addListener((p, o, n) -> {
      if (n) {
        if (rowNum + ROW_OFFSET == rows.size()) {
          rows.add(new ChartRow());
          editor.setUserData(false);
        }
      } else if (o && !n) {
        boolean edited = (Boolean) (editor.getUserData() == null ? false : editor.getUserData());
        if (edited) {
          triggerRedraw();
          editor.setUserData(false);
        }
      }
    });
    editor.textProperty().addListener((p, o, n) -> {
      chartRow.setValue(column, n);
      editor.setUserData(true);
    });
    editor.setOnKeyTyped(e -> {
      boolean selectNext = false;
      if (e.getCode() == KeyCode.UNDEFINED) {
        if (e.getCharacter().equals("\r")) {
          selectNext = true;
        }
      } else if (e.getCode() == KeyCode.ENTER) {
        selectNext = true;
      }
      if (selectNext) {
        int next = rowNum + 1;
        if (valueEditors.containsRow(next)) {
          TextField textField = valueEditors.row(next).get(column);
          textField.requestFocus();
        }
        e.consume();
      }
    });
    return editor;
  }

  protected void triggerRedraw() {
    if (callback != null && !validationRegistry.isInvalid()) {
      callback.accept(getData());
    }
  }

  @FXML
  void onAddColumn() {
    Optional<String> input = Dialogs.create().message(Localized.get("column.title")).showTextInput();
    if (input.isPresent()) {
      addColumnHeader(input.get());
    }
  }

  public void addColumnHeader(String title) {
    columnHeaders.add(new SimpleStringProperty(title));
  }

  public ObservableList<ChartRow> getRows() {
    return rows;
  }

  public ObservableList<SimpleStringProperty> getColumnHeaders() {
    return columnHeaders;
  }

  public List<TextField> getHeaders() {
    return headers;
  }

  public List<TextField> getCategoryEditors() {
    return categoryEditors;
  }

  public ChartPreviewData getData() {
    ChartPreviewData data = new ChartPreviewData();
    this.rows.forEach(r -> {
      data.getCategories().add(r.getCategory());
    });

    for (int i = 0; i < columnHeaders.size(); i++) {
      SimpleStringProperty header = columnHeaders.get(i);
      LinkedList<Double> values = new LinkedList<>();
      for (int rowNum = 0; rowNum < rows.size(); rowNum++) {
        ChartRow row = rows.get(rowNum);
        if (row.getCategory() == null) {
          continue;
        }
        String value = row.getValue(i);
        if (value != null) {
          double val = Double.parseDouble(value);
          values.add(val);
        } else {
          values.add(0d);
        }
      }
      data.addSeries(header.getValueSafe(), values);
    }
    data.setxAxisTitle(xaxisTitle.getText());
    return data;
  }

  public void setCallback(Consumer<ChartPreviewData> callback) {
    this.callback = callback;
  }
}