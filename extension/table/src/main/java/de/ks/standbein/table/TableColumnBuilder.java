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
package de.ks.standbein.table;

import de.ks.standbein.reflection.PropertyPath;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.scene.control.TableColumn;

import java.util.function.Function;
import java.util.function.Supplier;

public class TableColumnBuilder<TableType> {
  protected Function<TableType, ?> function;
  protected Supplier<? extends ObservableValue<?>> observableValueSupplier;
  protected Integer width;
  protected String name;
  protected Class<TableType> tableClass;
  protected PropertyPath propertyPath;

  public TableColumnBuilder<TableType> setFunction(Function<TableType, ?> function) {
    this.function = function;
    return this;
  }

  public <O extends ObservableValue<?> & WritableValue<?>> TableColumnBuilder<TableType> setValueSupplier(Supplier<O> observable) {
    this.observableValueSupplier = observable;
    return this;
  }

  public TableColumnBuilder<TableType> setWidth(Integer width) {
    this.width = width;
    return this;
  }

  public TableColumnBuilder<TableType> setName(String name) {
    this.name = name;
    return this;
  }

  public TableColumnBuilder<TableType> setTableClass(Class<TableType> tableClass) {
    this.tableClass = tableClass;
    return this;
  }

  public TableColumnBuilder<TableType> setPropertyPath(PropertyPath propertyPath) {
    this.propertyPath = propertyPath;
    return this;
  }

  @SuppressWarnings("unchecked")
  public TableColumn<TableType, ?> build() {
    TableColumn<TableType, ?> tableColumn = new TableColumn<>();
    tableColumn.setCellValueFactory(param -> {
      TableType item = param.getValue();
      Object value = function.apply(item);
      ObservableValue observableValue = observableValueSupplier.get();
      if (value != null) {
        ((WritableValue) observableValue).setValue(value);
      }
      return observableValue;
    });
    if (width != null) {
      tableColumn.setPrefWidth(width);
    }
    return tableColumn;
  }

  public Function<TableType, ?> getFunction() {
    return function;
  }

  @SuppressWarnings("unchecked")
  public <O extends ObservableValue<?> & WritableValue<?>> Supplier<O> getObservableValueSupplier() {
    return (Supplier<O>) observableValueSupplier;
  }

  public Integer getWidth() {
    return width;
  }

  public String getName() {
    return name;
  }

  public Class<TableType> getTableClass() {
    return tableClass;
  }

  public PropertyPath getPropertyPath() {
    return propertyPath;
  }
}
