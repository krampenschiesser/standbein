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

package de.ks.validation.validators;

import de.ks.i18n.Localized;
import de.ks.validation.ValidationResult;
import de.ks.validation.Validator;
import javafx.scene.control.Control;

import java.io.File;

public class FileExistsValidator implements Validator<Control, String> {
  @Override
  public ValidationResult apply(Control control, String s) {
    String filePath = s == null || s.isEmpty() ? "" : getFilePathFromString(s);

    String validationMsg = Localized.get("validation.fileDoesNotExist", filePath);
    ValidationResult validationResult = ValidationResult.createError(validationMsg);

    if (s == null || s.isEmpty()) {
      return validationResult;
    } else {
      File file = new File(filePath);
      if (!file.exists()) {
        return validationResult;
      }
    }
    return null;
  }

  protected String getFilePathFromString(String s) {
    return s;
  }
}
