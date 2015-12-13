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

public class IntegerRangeValidator extends IntegerValidator {
  protected final int minInclusive;
  protected final int maxExclusive;

  public IntegerRangeValidator(Localized localized, int minInclusive, int maxExclusive) {
    super(localized);
    this.minInclusive = minInclusive;
    this.maxExclusive = maxExclusive;
  }

  @Override
  protected ValidationResult furtherProcessing(Control control, Integer value) {
    if (value < minInclusive) {
      String validationMsg = localized.get("validation.number.greaterEquals", minInclusive);
      return ValidationResult.createError(validationMsg);
    } else if (value >= maxExclusive) {
      String validationMsg = localized.get("validation.number.lessThan", maxExclusive);
      return ValidationResult.createError(validationMsg);
    }
    return super.furtherProcessing(control, value);
  }
}
