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

import com.google.common.primitives.Primitives;
import de.ks.standbein.i18n.LocalizationModule;
import de.ks.standbein.i18n.Localized;
import de.ks.standbein.reflection.PropertyPath;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.scene.control.TableView;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class TableConfigurator<E> {
  private final DateTimeFormatter dateTimeFormatter;
  private final DateTimeFormatter dateFormatter;
  private final Localized localized;
  protected List<TableColumnBuilder<E, ?, ?, ?>> builders = new ArrayList<>();

  @Inject
  public TableConfigurator(@Named(LocalizationModule.DATETIME_FORMAT) DateTimeFormatter dateTimeFormatter,//
                           @Named(LocalizationModule.DATE_FORMAT) DateTimeFormatter dateFormatter,//
                           Localized localized) {
    this.dateTimeFormatter = dateTimeFormatter;
    this.dateFormatter = dateFormatter;
    this.localized = localized;
  }

  public TableColumnBuilder<E, String, String, SimpleStringProperty> addText(Class<E> clazz, Function<E, String> function) {
    return add(clazz, function, SimpleStringProperty::new);
  }

  public TableColumnBuilder<E, String, String, SimpleStringProperty> addDateTime(Class<E> clazz, Function<E, LocalDateTime> function) {
    Function<E, String> wrapper = e -> {
      LocalDateTime localDateTime = function.apply(e);
      return localDateTime == null ? null : dateTimeFormatter.format(localDateTime);
    };
    return add(clazz, wrapper, SimpleStringProperty::new);
  }

  public TableColumnBuilder<E, String, String, SimpleStringProperty> addDate(Class<E> clazz, Function<E, LocalDate> function) {
    Function<E, String> wrapper = e -> {
      LocalDate localDate = function.apply(e);
      return localDate == null ? null : dateFormatter.format(localDate);
    };
    return add(clazz, wrapper, SimpleStringProperty::new);
  }

  @SuppressWarnings("unchecked")
  public TableColumnBuilder<E, Number, Number, ?> addNumber(Class<E> clazz, Function<E, ? extends Number> function) {
    PropertyPath propertyPath = PropertyPath.ofTypeSafe(clazz, function);
    Class<?> returnType = propertyPath.getReturnType();
    returnType = Primitives.wrap(returnType);
    if (Integer.class.equals(returnType) || Short.class.equals(returnType) || Byte.class.equals(returnType)) {
      return add(clazz, (Function<E, Number>) function, SimpleIntegerProperty::new);
    }
    if (Long.class.equals(returnType)) {
      return add(clazz, (Function<E, Number>) function, SimpleLongProperty::new);
    }
    if (Float.class.equals(returnType)) {
      return add(clazz, (Function<E, Number>) function, SimpleFloatProperty::new);
    }
    if (Double.class.equals(returnType)) {
      return add(clazz, (Function<E, Number>) function, SimpleDoubleProperty::new);
    }
    throw new IllegalArgumentException("Unkown number type " + returnType);
  }

  public void clear() {
    builders.clear();
  }

  public List<TableColumnBuilder<E, ?, ?, ?>> getBuilders() {
    return builders;
  }

  public void configureTable(TableView<E> tableView) {
    builders.stream().sequential()//
      .map(TableColumnBuilder::build)//
      .forEach(e -> tableView.getColumns().add(e));
  }

  private <V, D, O extends ObservableValue<D> & WritableValue<D>> TableColumnBuilder<E, V, D, O> add(Class<E> clazz, //
                                                                                                     Function<E, V> function, Supplier<O> valueSupplier) {
    PropertyPath path = PropertyPath.ofTypeSafe(clazz, function);
    String name = localized.get(path.getPropertyPath());

    TableColumnBuilder<E, V, D, O> builder = new TableColumnBuilder<>();
    builder.setFunction(function);
    builder.setValueSupplier(valueSupplier);
    builder.setName(name);
    builder.setTableClass(clazz);
    builder.setPropertyPath(path);
    builders.add(builder);
    return builder;
  }
}
