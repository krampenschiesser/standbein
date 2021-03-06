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
package de.ks.standbein.validation.cell;

import de.ks.standbein.validation.ValidationRegistry;
import de.ks.standbein.validation.ValidationResult;
import de.ks.standbein.validation.Validator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;

public class ValidatingTableCell<S, T> extends TableCell<S, T> {
  protected final List<Validator<Control, String>> validators;
  private final ValidationRegistry validationRegistry;
  protected TextField textField;

  public ValidatingTableCell(ValidationRegistry validationRegistry, StringConverter<T> converter, Validator<Control, String>... validators) {
    this.validationRegistry = validationRegistry;
    this.getStyleClass().add("text-field-table-cell");
    setConverter(converter);
    this.validators = Arrays.asList(validators);
  }

  /**
   * ************************************************************************
   * *
   * Properties                                                              *
   * *
   * ************************************************************************
   */

  // --- converter
  private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");

  /**
   * The {@link StringConverter} property.
   */
  public final ObjectProperty<StringConverter<T>> converterProperty() {
    return converter;
  }

  /**
   * Sets the {@link StringConverter} to be used in this cell.
   */
  public final void setConverter(StringConverter<T> value) {
    converterProperty().set(value);
  }

  /**
   * Returns the {@link StringConverter} used in this cell.
   */
  public final StringConverter<T> getConverter() {
    return converterProperty().get();
  }


  /***************************************************************************
   *                                                                         *
   * Public API                                                              *
   *                                                                         *
   **************************************************************************/

  /**
   * {@inheritDoc}
   */
  @Override
  public void startEdit() {
    if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
      return;
    }
    super.startEdit();

    if (isEditing()) {
      if (textField == null) {
        textField = createTextField(validationRegistry, this, getConverter(), validators);
      }

      startEdit(this, getConverter(), null, null, textField);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cancelEdit() {
    super.cancelEdit();
    cancelEdit(this, getConverter(), null);
    textField.setText(getItemText(this, getConverter()));
  }

  @Override
  public void commitEdit(T newValue) {
    ValidationResult validationResult = validationRegistry.getValidationResult(textField);
    if (validationResult == null || validationResult.getMessages().isEmpty()) {
      super.commitEdit(newValue);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void updateItem(T item, boolean empty) {
    super.updateItem(item, empty);

    updateItem(this, getConverter(), null, null, textField);
  }

  static <T> void updateItem(final Cell<T> cell, final StringConverter<T> converter, final HBox hbox, final Node graphic, final TextField textField) {
    if (cell.isEmpty()) {
      cell.setText(null);
      cell.setGraphic(null);
    } else {
      if (cell.isEditing()) {
        if (textField != null) {
          textField.setText(getItemText(cell, converter));
        }
        cell.setText(null);

        if (graphic != null) {
          hbox.getChildren().setAll(graphic, textField);
          cell.setGraphic(hbox);
        } else {
          cell.setGraphic(textField);
        }
      } else {
        cell.setText(getItemText(cell, converter));
        cell.setGraphic(graphic);
      }
    }
  }

  static <T> void cancelEdit(Cell<T> cell, final StringConverter<T> converter, Node graphic) {
    cell.setText(getItemText(cell, converter));
    cell.setGraphic(graphic);
  }

  static <T> TextField createTextField(ValidationRegistry validationRegistry, final Cell<T> cell, final StringConverter<T> converter, List<Validator<Control, String>> validators) {
    final TextField textField = new TextField(getItemText(cell, converter));
    validationRegistry.registerValidator(textField, validators.stream().reduce((first, second) -> first.and(second)).get());

    // Use onAction here rather than onKeyReleased (with check for Enter),
    // as otherwise we encounter RT-34685
    textField.setOnAction(event -> {
      if (converter == null) {
        throw new IllegalStateException("Attempting to convert text input into Object, but provided " + "StringConverter is null. Be sure to set a StringConverter " + "in your cell factory.");
      }
      ValidationResult validationResult = validationRegistry.getValidationResult(textField);
      if (validationResult == null || validationResult.getMessages().isEmpty()) {
        cell.commitEdit(converter.fromString(textField.getText()));
      }
      event.consume();
    });
    textField.setOnKeyReleased(t -> {
      if (t.getCode() == KeyCode.ESCAPE) {
        cell.cancelEdit();
        t.consume();
      }
    });
    return textField;
  }

  private static <T> String getItemText(Cell<T> cell, StringConverter<T> converter) {
    return converter == null ? cell.getItem() == null ? "" : cell.getItem().toString() : converter.toString(cell.getItem());
  }

  static <T> void startEdit(final Cell<T> cell, final StringConverter<T> converter, final HBox hbox, final Node graphic, final TextField textField) {
    if (textField != null) {
      textField.setText(getItemText(cell, converter));
    }
    cell.setText(null);

    if (graphic != null) {
      hbox.getChildren().setAll(graphic, textField);
      cell.setGraphic(hbox);
    } else {
      cell.setGraphic(textField);
    }

    textField.selectAll();

    // requesting focus so that key input can immediately go into the
    // TextField (see RT-28132)
    textField.requestFocus();
  }

}
