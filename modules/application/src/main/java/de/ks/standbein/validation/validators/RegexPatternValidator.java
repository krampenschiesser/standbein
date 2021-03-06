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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexPatternValidator extends LocalizedValidator<Control, String> {
  public RegexPatternValidator(Localized localized) {
    super(localized);
  }

  @Override
  public ValidationResult apply(Control control, String s) {
    if (s == null || s.isEmpty()) {
      return null;
    } else {
      try {
        Pattern.compile(s);
        return null;
      } catch (PatternSyntaxException e) {
        return ValidationResult.createError(localized.get("invalid.pattern", s));
      }
    }
  }
}
