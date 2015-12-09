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
package de.ks.validation;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;

import java.util.function.Function;

public class DefaultValueProvider implements Function<Control, ObservableValue> {
  @Override
  public ObservableValue apply(Control control) {
    if (control instanceof TextField) {
      return ((TextField) control).textProperty();
    }
    if (control instanceof TextArea) {
      return ((TextArea) control).textProperty();
    }
    if (control instanceof ComboBox) {
      return ((ComboBox) control).getSelectionModel().selectedItemProperty();
    }
    if (control instanceof ListView) {
      return ((ListView) control).getSelectionModel().selectedItemProperty();
    }
    if (control instanceof TableView) {
      return ((TableView) control).getSelectionModel().selectedItemProperty();
    }
    if (control instanceof CheckBox) {
      return ((CheckBox) control).selectedProperty();
    }
    if (control instanceof DatePicker) {
      return ((DatePicker) control).valueProperty();
    }
    if (control instanceof RadioButton) {
      return ((RadioButton) control).selectedProperty();
    }
    if (control instanceof Slider) {
      return ((Slider) control).valueProperty();
    }
    if (control instanceof TreeView) {
      return ((TreeView) control).getSelectionModel().selectedItemProperty();
    }
    throw new IllegalArgumentException("Unkown control " + control.getClass() + " please use different register method with value provider.");
  }
}
