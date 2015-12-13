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
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class OtherLocalizationTest {

  private Injector injector;
  private Localized localized;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(new OtherLocalizationModule(), new LocalizationModule());
    localized = injector.getInstance(Localized.class);
  }

  @Test(timeout = 1000)
  public void testGetKey() throws Exception {
    localized.changeLocale(Locale.ENGLISH);
    String hello = localized.get("hello");
    assertEquals("Murks", hello);
  }

  private static class OtherLocalizationModule extends AbstractModule {
    @Override
    protected void configure() {
      OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named(LocalizationModule.FILENAME)))//
        .setBinding().toInstance("OtherLocalization");
      OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named(LocalizationModule.BASENAME)))//
        .setBinding().toInstance("de.ks.other.OtherLocalization");
    }
  }
}
