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
import de.ks.standbein.validation.LocalizedValidationMessage;
import de.ks.standbein.validation.ValidationResult;
import javafx.scene.control.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DurationValidator extends LocalizedValidator<Control, String> {
  private static final Logger log = LoggerFactory.getLogger(DurationValidator.class);
  private final String minutesSuffixShort;
  private final String minutesSuffifx;
  private final String hoursSuffix;
  private final String hoursSuffixShort;
  private final String durationFormat;
  private final AtomicReference<Duration> duration = new AtomicReference<>();

  public DurationValidator(Localized localized) {
    super(localized);
    minutesSuffifx = localized.get("duration.minutes");
    minutesSuffixShort = localized.get("duration.minutes.short");
    hoursSuffix = localized.get("duration.hours");
    hoursSuffixShort = localized.get("duration.hours.short");
    durationFormat = localized.get("duration.format");
  }

  @Override
  public ValidationResult apply(Control control, String timeString) {
    if (timeString == null) {
      duration.set(null);
      return null;
    } else if (timeString.trim().isEmpty()) {
      duration.set(null);
      return null;
    } else {
      if (timeString.contains(":")) {
        return parseFormat(timeString);
      } else if (hasSuffix(timeString, minutesSuffixShort, minutesSuffifx, TimeUnit.MINUTES)) {
        return null;
      } else if (hasSuffix(timeString, hoursSuffixShort, hoursSuffix, TimeUnit.HOURS)) {
        return null;
      }
      return new ValidationResult().add(new LocalizedValidationMessage(localized, "validation.duration", minutesSuffifx, minutesSuffixShort, hoursSuffix, hoursSuffixShort, durationFormat));
    }
  }

  private ValidationResult parseFormat(String timeString) {
    ValidationResult result = new ValidationResult().add(new LocalizedValidationMessage(localized, "validation.duration.invalidFormat", durationFormat));

    String[] split = timeString.split(":");
    if (split.length != 2) {
      duration.set(null);
      return result;
    }

    String hourString = split[0];
    String minutesString = split[1];
    if (minutesString.length() < 2) {
      duration.set(null);
      return result;
    }

    try {
      int hours = Integer.valueOf(hourString);
      int minutes = Integer.valueOf(minutesString);
      if (minutes > 59) {
        duration.set(null);
        return result;
      }
      long total = TimeUnit.HOURS.toMinutes(hours) + minutes;
      duration.set(Duration.ofMinutes(total));
    } catch (NumberFormatException e) {
      duration.set(null);
      return result;
    }
    return null;
  }

  protected boolean hasSuffix(String timeString, String shortSuffix, String longSuffix, TimeUnit timeUnit) {
    int shortIndex = timeString.trim().indexOf(shortSuffix);
    int longIndex = timeString.trim().indexOf(longSuffix);

    if (longIndex >= 0) {
      timeString = timeString.substring(0, longIndex);
    } else if (shortIndex >= 0) {
      timeString = timeString.substring(0, shortIndex);
    }
    boolean hasTimeUnit = shortIndex >= 0 || longIndex >= 0;
    if (!hasTimeUnit) {
      duration.set(null);
      return false;
    }
    try {
      int amount = Integer.parseInt(timeString.trim());
      long minutes = timeUnit.toMinutes(amount);
      duration.set(Duration.ofMinutes(minutes));
      return true;
    } catch (NumberFormatException e) {
      duration.set(null);
      return false;
    }
  }

  public Duration getDuration() {
    return duration.get();
  }
}
