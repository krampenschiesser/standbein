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
package de.ks.i18n;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationModule extends AbstractModule {
  public static final String FILENAME = "ResourceBundle.filename";
  public static final String BASENAME = "ResourceBundle.baseName";

  @Override
  protected void configure() {
    bind(String.class).annotatedWith(Names.named(FILENAME)).toInstance("Translation");
    bind(String.class).annotatedWith(Names.named(BASENAME)).toInstance("de.ks.i18n.Translation");
    bind(Locale.class).toInstance(Locale.getDefault());
  }

  @Provides
  @Singleton
  @Inject
  public ResourceBundle getResourceBundle(Localized localized) {
    return localized.bundle;
  }
}
