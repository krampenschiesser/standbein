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
package de.ks.validation.sample;

import de.ks.validation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

  @FXML
  private Button button;

  @FXML
  private TextField decoratedField;

  @FXML
  private TextField validatedField;
  private ValidationContainer validationContainer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ValidationResult result = new ValidationResult();
    result.add(new ValidationMessage("Just a decorated field"));
    Platform.runLater(() -> new DefaultDecorator().decorate(decoratedField, result));

    validationContainer = new ValidationContainer();


    Validator<TextField, String> emptyValidator = (textField, s) -> {
      if (s == null || s.isEmpty()) {
        return ValidationResult.createError("Must not be empty!.");
      } else {
        return null;
      }
    };
    Validator<TextField, String> numberValidator = (textField, s) -> {
      try {
        int number = Integer.parseInt(s);
        if (number < 0 || number > 42) {
          return ValidationResult.createError("Number '" + number + "' out of range. Has to be between 0 and 42");
        }
      } catch (NumberFormatException e) {
        return ValidationResult.createError("No valid number '" + s + "'");
      }
      return null;
    };
    validationContainer.registerValidator(validatedField, emptyValidator.and(numberValidator));

    button.disableProperty().bind(validationContainer.invalidProperty());
  }
}
