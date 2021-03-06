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
package de.ks.standbein.validation.validators;

import de.ks.standbein.i18n.Localized;
import de.ks.standbein.validation.ValidationResult;
import javafx.scene.control.Control;

import java.time.format.DateTimeFormatter;

public class DateTimeFormatterPatternValidator extends LocalizedValidator<Control, String> {

  public DateTimeFormatterPatternValidator(Localized localized) {
    super(localized);
  }

  @Override
  public ValidationResult apply(Control control, String s) {
    if (s == null || s.isEmpty()) {
      return null;
    }
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(s);
      return null;
    } catch (IllegalArgumentException e) {
      return invalid(control, s);
    }
  }

  protected ValidationResult invalid(Control control, String s) {
    String msg = localized.get("validation.invalidDatePattern", s);
    return ValidationResult.createError(msg);
  }
}
