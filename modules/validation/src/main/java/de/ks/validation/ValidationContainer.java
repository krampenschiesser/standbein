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

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Only one validator allowed for control -> only one result per control
 */
public class ValidationContainer {
  private static final Logger log = LoggerFactory.getLogger(ValidationContainer.class);
  protected final DefaultValueProvider valueProvider = new DefaultValueProvider();

  protected final Map<Control, Validator> validators = new HashMap<>();
  protected final Map<Control, ObservableValue> observableValues = new HashMap<>();

  protected final ObservableMap<Control, ValidationResult> results = FXCollections.observableHashMap();
  protected final Map<ObservableValue, ChangeListener> registeredObservers = new HashMap<>();
  protected final Map<Control, ControlDecorator> decorators = new HashMap<>();

  protected final SimpleObjectProperty<ControlDecorator> defaultDecorator = new SimpleObjectProperty<>(new DefaultDecorator());
  protected final SimpleBooleanProperty invalid = new SimpleBooleanProperty();
  protected final SimpleObjectProperty<ValidationResult> validationResult = new SimpleObjectProperty<>();

  private final ExecutorService javaFXExecutor;

  public ValidationContainer() {
    this(null);
  }

  public ValidationContainer(ExecutorService javaFXExecutor) {
    this.javaFXExecutor = javaFXExecutor;
    results.addListener((MapChangeListener<Control, ValidationResult>) change -> {
      ValidationResult reduce = results.values().stream().reduce(new ValidationResult(), ValidationResult::combine);
      if (reduce.getMessages().size() > 0) {
        validationResult.set(reduce);
        invalid.set(true);
      } else {
        validationResult.set(null);
        invalid.set(false);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public <C extends Control, T> void registerValidator(C control, Validator<C, T> validator) {
    ObservableValue observableValue = valueProvider.apply(control);
    registerValidator(control, validator, observableValue);
  }

  @SuppressWarnings("unchecked")
  public <C extends Control, T> void registerValidator(C control, Validator<C, T> validator, ObservableValue<T> observableValue) {
    Validator existing = validators.get(control);
    if (existing != null) {
      validator = existing.and((Validator) validator);
    }
    validators.put(control, validator);
    observableValues.put(control, observableValue);
    configure(validator, control, observableValue);
    revalidate(control);
  }

  protected <C extends Control, T> void configure(Validator<C, T> validator, C control, ObservableValue<T> observableValue) {
    ChangeListener removed = registeredObservers.remove(observableValue);//remove existing
    if (removed != null) {
      observableValue.removeListener(removed);
    }

    ChangeListener<T> changeListener = (observable, oldValue, newValue) -> {
      runInFX(() -> handleChange(validator, control, newValue));
    };
    observableValue.addListener(changeListener);
    registeredObservers.put(observableValue, changeListener);
  }

  protected void runInFX(Runnable r) {
    if (javaFXExecutor == null) {
      Platform.runLater(r);
    } else {
      javaFXExecutor.submit(r);
    }
  }

  @SuppressWarnings("unchecked")
  protected void handleChange(Validator validator, Control control, Object newValue) {
    ValidationResult result = (ValidationResult) validator.apply(control, newValue);
    if (result != null && result.getMessages().isEmpty()) {
      result = null;
    }
    if (result != null) {
      results.put(control, result);
    } else {
      results.remove(control);
    }
    ControlDecorator decorator = decorators.getOrDefault(control, defaultDecorator.get());

    if (decorator != null) {
      if (result != null) {
        result.setValue(newValue);
        decorator.decorate(control, result);
      } else {
        decorator.removeDecoration(control);
      }
    }
  }

  public boolean getInvalid() {
    return invalid.get();
  }

  public ReadOnlyBooleanProperty invalidProperty() {
    return invalid;
  }

  public ValidationResult getValidationResult() {
    ValidationResult current = this.validationResult.get();
    return current == null ? null : current.clone();
  }

  public ReadOnlyObjectProperty<ValidationResult> validationResultProperty() {
    return validationResult;
  }

  public ControlDecorator getDefaultDecorator() {
    return defaultDecorator.get();
  }

  public SimpleObjectProperty<ControlDecorator> defaultDecoratorProperty() {
    return defaultDecorator;
  }

  public void setDefaultDecorator(ControlDecorator decorator) {
    this.defaultDecorator.set(decorator);
    validators.keySet().forEach(this::revalidate);
  }

  public void registerDecoratorForControl(Control control, ControlDecorator decorator) {
    decorators.put(control, decorator);
    revalidate(control);
  }

  private void revalidate(Control control) {
    Validator validator = validators.get(control);
    Object newValue = observableValues.get(control).getValue();
    runInFX(() -> handleChange(validator, control, newValue));
  }
}
