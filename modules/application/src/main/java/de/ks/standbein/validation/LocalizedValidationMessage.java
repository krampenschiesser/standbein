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

import de.ks.standbein.i18n.Localized;

public class LocalizedValidationMessage extends ValidationMessage {
  private final String messageTemplate;
  private final Object[] parameters;
  private final Localized localized;

  public LocalizedValidationMessage(Localized localized, String messageTemplate, Object... parameters) {
    this(localized, messageTemplate, Severity.ERROR, parameters);
  }

  public LocalizedValidationMessage(Localized localized, String messageTemplate, Severity severity, Object... parameters) {
    super(messageTemplate, severity);
    this.localized = localized;
    assert messageTemplate != null;
    this.messageTemplate = messageTemplate.replaceAll("\\{", "").replaceAll("\\}", "");
    this.parameters = parameters == null ? new Object[0] : parameters;
  }

  @Override
  public String getText() {
    if (messageTemplate != null) {
      return localized.get(messageTemplate, parameters);
    } else {
      return null;
    }
  }

}
