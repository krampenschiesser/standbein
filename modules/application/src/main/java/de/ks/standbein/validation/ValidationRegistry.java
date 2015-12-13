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
package de.ks.standbein.validation;

import de.ks.standbein.activity.context.ActivityScoped;
import de.ks.standbein.activity.context.ActivityStore;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Optional;

@ActivityScoped
public class ValidationRegistry {
  private static final Logger log = LoggerFactory.getLogger(ValidationRegistry.class);

  protected final SimpleBooleanProperty invalid = new SimpleBooleanProperty(false);

  protected final ActivityStore store;
  protected final ValidationContainer validationSupport;

  @Inject
  public ValidationRegistry(ValidationContainer validationSupport, ActivityStore store) {
    this.validationSupport = validationSupport;
    this.store = store;
  }

  @PostConstruct
  public void init() {
    validationSupport.invalidProperty().addListener((p, o, n) -> {
      invalid.set(n || store.isLoading());
      log.trace("Validation is {}", invalid.get() ? "Invalid" : "valid");
    });
    store.loadingProperty().addListener((p, o, n) -> {
      invalid.set(n || validationSupport.isInvalid());
      log.trace("Validation is {}", invalid.get() ? "Invalid" : "valid");
    });
  }

  public ValidationResult getValidationResult() {
    return validationSupport.getValidationResult();
  }

  public ValidationResult getValidationResult(Control target) {
    return validationSupport.getValidationResult(target);
  }

  public boolean isInvalid() {
    return invalid.get();
  }

  public boolean isValid() {
    return !invalid.get();
  }

  public ReadOnlyBooleanProperty invalidProperty() {
    return invalid;
  }

  public <C extends Control, T> void registerValidator(C control, Validator<C, T> validator) {
    validationSupport.registerValidator(control, validator);
  }

  public Optional<ValidationMessage> getHighestMessage(Control target) {
    return validationSupport.getHighestMessage(target);
  }
}
