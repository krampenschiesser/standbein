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

package de.ks.persistence.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDateTime;

/**
 *
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {
  private static final Logger log = LoggerFactory.getLogger(LocalDateTimeConverter.class);

  @Override
  public String convertToDatabaseColumn(LocalDateTime attribute) {
    if (attribute == null) {
      return null;
    } else {
      return attribute.toString();
    }

  }

  @Override
  public LocalDateTime convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    } else {
      return LocalDateTime.parse(dbData);
    }
  }
}
