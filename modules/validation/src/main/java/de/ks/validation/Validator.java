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

import javafx.scene.control.Control;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@FunctionalInterface
public interface Validator<C extends Control, V> extends BiFunction<C, V, ValidationResult> {

  default Validator<C, V> and(Validator<C, V> other) {
    if (this instanceof CombinedValidator) {
      return ((CombinedValidator<C, V>) this).addValidator(other);
    } else {
      CombinedValidator<C, V> combined = new CombinedValidator<>();
      combined.addValidator(this);
      combined.addValidator(other);
      return combined;
    }
  }

  static class CombinedValidator<C extends Control, V> implements Validator<C, V> {
    final List<Validator<C, V>> validators = new ArrayList<>();

    public CombinedValidator<C, V> addValidator(Validator<C, V> validator) {
      validators.add(validator);
      return this;
    }

    @Override
    public ValidationResult apply(C control, V value) {
      return validators.stream().map(v -> v.apply(control, value)).reduce(new ValidationResult(), ValidationResult::combine);
    }
  }
}
