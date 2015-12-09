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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ValidationResult implements Cloneable {
  public static ValidationResult createError(String msg) {
    ValidationResult result = new ValidationResult();
    result.add(new ValidationMessage(msg));
    return result;
  }

  public static ValidationResult createWarning(String msg) {
    ValidationResult result = new ValidationResult();
    result.add(new ValidationMessage(msg, Severity.WARNING));
    return result;
  }

  private List<ValidationMessage> messages = new ArrayList<>();
  protected Object value;

  public ValidationResult add(ValidationMessage message) {
    if (message != null) {
      messages.add(message);
    }
    return this;
  }

  public List<ValidationMessage> getMessages() {
    return messages;
  }

  @SuppressWarnings("unchecked")
  public <E> E getValue() {
    return (E) value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public ValidationResult combine(ValidationResult other) {
    if (other == null) {
      return this;
    }
    ValidationResult retval = clone();
    retval.messages.addAll(other.messages);
    return retval;
  }

  public ValidationMessage getHighestMessage() {
    if (messages.isEmpty()) {
      throw new IllegalStateException("neeee");
    } else {
    return messages.stream().max(Comparator.comparing(m -> m.getSeverity().ordinal())).get();
    }
  }

  @Override
  public ValidationResult clone() {
    try {
      ValidationResult value = (ValidationResult) super.clone();
      value.messages = new ArrayList<>(messages);
      return value;
    } catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }
}
