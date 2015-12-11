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

package de.ks.i18n;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ks.LauncherRunner;
import de.ks.eventsystem.EventBusModule;
import de.ks.eventsystem.bus.EventBus;
import de.ks.i18n.event.LanguageChangedEvent;
import de.ks.i18n.nobundle.NoBundleClass;
import de.ks.i18n.other.OtherClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */
@RunWith(LauncherRunner.class)
public class LocalizedTest {
  private LanguageChangedEvent event;
  private Localized localized;
  private EventBus eventBus;

  @Before
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new LocalizationModule(), new EventBusModule());
    localized = injector.getInstance(Localized.class);
    localized.changeLocale(Locale.ENGLISH);
    eventBus = injector.getInstance(EventBus.class);
  }

  @Test
  public void testLanguageChange() throws Exception {
    String helloWorld = localized.get("hello");
    assertEquals("Hello world!", helloWorld);

    localized.changeLocale(Locale.GERMAN);
    helloWorld = localized.get("hello");
    assertEquals("Hallo Welt!", helloWorld);

    localized.changeLocale(Locale.ENGLISH);
    helloWorld = localized.get("hello");
    assertEquals("Hello world!", helloWorld);
  }

  @Test
  public void testParameters() throws Exception {
    String helloSauerland = localized.get("hello.parametererized", "Sauerland", "!!!");
    assertEquals("Hello Sauerland!!!", helloSauerland);
  }

  @Test
  public void testParametersPositioned() throws Exception {
    String helloSauerland = localized.get("hello.positioned", "!!!", "Sauerland");
    assertEquals("Hello Sauerland!!!", helloSauerland);
  }

  @Test
  public void testLanguageChangeEvent() throws Exception {
    eventBus.register(this);
    try {
      localized.changeLocale(Locale.GERMAN);
      assertNotNull(event);
      assertEquals(Locale.ENGLISH, event.getOldLocale());
      assertEquals(Locale.GERMAN, event.getNewLocale());
    } finally {
      eventBus.unregister(this);
    }
  }

  @Test
  public void testMissingKeys() throws Exception {
    localized.get("doesNot.Exist.Dots");
    localized.get("doesNotExistNoDots");

    File missingKeyFile = localized.getBundle().getMissingKeyFile();
    assertNotNull(missingKeyFile);
    List<String> missing = Files.readLines(missingKeyFile, Charsets.UTF_8);
    List<String> extracted = missing.stream().filter((s) -> s.startsWith("doesNot")).collect(Collectors.toList());
    assertEquals(2, extracted.size());

    String property = extracted.stream().filter((s) -> s.startsWith("doesNot.")).findFirst().get();
    assertEquals("doesNot.Exist.Dots = Dots", property);

    property = extracted.stream().filter((s) -> s.startsWith("doesNotE")).findFirst().get();
    assertEquals("doesNotExistNoDots = doesNotExistNoDots", property);
  }

  @Test
  public void testNullParameter() throws Exception {
    localized.get("doesNot.Exist.Dots", (Object[]) null);
  }

  @Test
  public void testNullParameterArray() throws Exception {
    localized.get("doesNot.Exist.Dots", null, null);
  }

  @Subscribe
  public void onLanguageChange(LanguageChangedEvent event) {
    this.event = event;
  }

  @Test
  public void testSubPackageKey() throws Exception {
    Supplier<String> supplier = () -> new OtherClass(localized).getString();
    assertEquals("Hello SubPackage", supplier.get());
    assertEquals("Hello SubPackage", new OtherClass(localized).getString());
  }

  @Test
  public void testNoSubPackageKey() throws Exception {
    assertEquals("?subPackageString?", new NoBundleClass(localized).getString());
  }

  @Test
  public void testRootSubPackageKey() throws Exception {
    assertEquals("?subPackageString?", localized.get("subPackageString"));
  }
}
