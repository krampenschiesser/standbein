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

package de.ks.standbein.validation.validators;

import de.ks.standbein.i18n.Localized;
import de.ks.standbein.validation.ValidationResult;
import javafx.scene.control.Control;

import java.util.function.Function;

public class BaseNumberValidator<T extends Number> extends LocalizedValidator<Control, String> {
  protected final Function<String, T> parser;
  protected final String msg;

  public BaseNumberValidator(Localized localized, Function<String, T> parser, String msg) {
    super(localized);
    this.parser = parser;
    this.msg = msg;
  }

  @Override
  public ValidationResult apply(Control control, String s) {
    if (s == null || s.isEmpty()) {
      return null;
    }
    try {
      T value = parser.apply(s);
      return furtherProcessing(control, value);
    } catch (NumberFormatException e) {
      String validationMsg = localized.get(msg);
      return ValidationResult.createError(validationMsg);
    }
  }

  protected ValidationResult furtherProcessing(Control control, T value) {
    return null;
  }
}
