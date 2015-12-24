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
package de.ks.standbein.i18n;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationModule extends AbstractModule {
  public static final String DATETIME_FORMAT = "dateTimeFormat";
  public static final String DATE_FORMAT = "dateFormat";

  public static final String FILENAME = "ResourceBundle.filename";
  public static final String BASENAME = "ResourceBundle.baseName";

  @Override
  protected void configure() {
    OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named(FILENAME)))//
      .setDefault().toInstance("Translation");
    OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named(BASENAME)))//
      .setDefault().toInstance("de.ks.standbein.i18n.Translation");

    OptionalBinder.newOptionalBinder(binder(), Locale.class).setDefault().toInstance(Locale.getDefault());

    bind(DateTimeFormatter.class).annotatedWith(Names.named(DATETIME_FORMAT)).toInstance(DateTimeFormatter.ofPattern("uuu.MM.dd HH.mm.ss"));
    bind(DateTimeFormatter.class).annotatedWith(Names.named(DATE_FORMAT)).toInstance(DateTimeFormatter.ofPattern("uuu.MM.dd"));
  }

  @Provides
  @Singleton
  @Inject
  public ResourceBundle getResourceBundle(Localized localized) {
    return localized.getBundle();
  }
}
