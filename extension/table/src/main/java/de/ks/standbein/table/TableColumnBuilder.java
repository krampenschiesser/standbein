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
import javafx.scene.control.TreeTableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TableColumnBuilder<TableType> {
  private static final Logger log = LoggerFactory.getLogger(TableColumnBuilder.class);
  protected Function<TableType, ?> function;
  protected Supplier<? extends ObservableValue<?>> observableValueSupplier;
  protected Integer width;
  protected String name;
  protected Class<TableType> tableClass;
  protected PropertyPath propertyPath;
  protected Consumer<TableColumn<TableType, ?>> tablePostProcessor;
  protected Consumer<TreeTableColumn<TableType, ?>> treePostProcessor;

  public TableColumnBuilder<TableType> setFunction(Function<TableType, ?> function) {
    this.function = function;
    return this;
  }

  public <V, O extends ObservableValue<V> & WritableValue<V>> TableColumnBuilder<TableType> setValueSupplier(Supplier<O> observable) {
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

  public TableColumnBuilder<TableType> setTablePostProcessor(Consumer<TableColumn<TableType, ?>> tablePostProcessor) {
    this.tablePostProcessor = tablePostProcessor;
    return this;
  }

  public TableColumnBuilder<TableType> setTewwPostProcessor(Consumer<TreeTableColumn<TableType, ?>> treePostProcessor) {
    this.treePostProcessor = treePostProcessor;
    return this;
  }

  @SuppressWarnings("unchecked")
  public TableColumn<TableType, ?> buildTableColumn() {
    TableColumn<TableType, ?> tableColumn = new TableColumn<>(name);
    tableColumn.setCellValueFactory(param -> {
      TableType item = param.getValue();
      Object value = null;
      try {
        value = function.apply(item);
      } catch (NullPointerException e) {
        log.debug("Could not get value from {}", item, e);
      }
      ObservableValue observableValue = observableValueSupplier.get();
      if (value != null) {
        ((WritableValue) observableValue).setValue(value);
      }
      return observableValue;
    });
    if (width != null) {
      tableColumn.setPrefWidth(width);
    }
    if (tablePostProcessor != null) {
      tablePostProcessor.accept(tableColumn);
    }
    return tableColumn;
  }

  @SuppressWarnings("unchecked")
  public TreeTableColumn<TableType, ?> buildTreeTableColumn() {
    TreeTableColumn<TableType, ?> tableColumn = new TreeTableColumn<>(name);
    tableColumn.setCellValueFactory(param -> {
      TableType item = param.getValue().getValue();
      Object value = null;
      try {
        value = function.apply(item);
      } catch (NullPointerException e) {
        log.debug("Could not get value from {}", item, e);
      }
      ObservableValue observableValue = observableValueSupplier.get();
      if (value != null) {
        ((WritableValue) observableValue).setValue(value);
      }
      return observableValue;
    });
    if (width != null) {
      tableColumn.setPrefWidth(width);
    }
    if (treePostProcessor != null) {
      treePostProcessor.accept(tableColumn);
    }
    return tableColumn;
  }

  public Function<TableType, ?> getFunction() {
    return function;
  }

  @SuppressWarnings("unchecked")
  public <V, O extends ObservableValue<V> & WritableValue<V>> Supplier<O> getObservableValueSupplier() {
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

  public Consumer<TableColumn<TableType, ?>> getTablePostProcessor() {
    return tablePostProcessor;
  }

  public Consumer<TreeTableColumn<TableType, ?>> getTreePostProcessor() {
    return treePostProcessor;
  }
}
