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

package de.ks.standbein.i18n;

import de.ks.eventsystem.bus.EventBus;
import de.ks.standbein.i18n.event.LanguageChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import static de.ks.standbein.i18n.LocalizationModule.*;

/**
 * Main interface to the i18n facilities.
 */
@Singleton
public class Localized {
  private static final Logger log = LoggerFactory.getLogger(Localized.class);

  protected UTF8Control control = new UTF8Control();
  protected ResourceBundleWrapper bundle;

  protected final String fileName;
  protected final String baseName;

  EventBus eventBus;
  protected Locale locale;

  @Inject
  public Localized(Locale locale, @Named(FILENAME) String fileName, @Named(BASENAME) String baseName) {
    this.locale = locale;
    this.fileName = fileName;
    this.baseName = baseName;
  }

  @Inject
  protected void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  protected void initialize() {
    String path = baseName + "_" + locale.getLanguage() + ".properties";
    bundle = new ResourceBundleWrapper(fileName, ResourceBundle.getBundle(baseName, locale, control), null, path, locale);
  }

  private boolean isInitialized() {
    return bundle != null;
  }

  /**
   * Use this method in order to notify possible listeners and replace the resource bundle.
   *
   * @param newLocale
   */
  public void changeLocale(Locale newLocale) {
    Locale oldLocale = locale;
    locale = newLocale;
    initialize();
    if (eventBus != null) {
      eventBus.post(new LanguageChangedEvent(oldLocale, newLocale));
    }
  }

  /**
   * @return the currently used bundle to use for eg. JavaFX loaders etc.
   */
  public ResourceBundleWrapper getBundle() {
    if (!isInitialized()) {
      initialize();
    }
    return bundle;
  }

  public ResourceBundleWrapper getBundle(Class callerClass) {
    if (!isInitialized()) {
      initialize();
    }
    String substring = callerClass.getName().substring(0, callerClass.getName().lastIndexOf('.') + 1);


    String classRelativeBaseName = substring + fileName;
    URL resource = callerClass.getResource("/" + control.getResourceName(classRelativeBaseName, locale));
    if (resource == null) {
      resource = callerClass.getResource("/" + control.getResourceName(classRelativeBaseName, control.getFallbackLocale(classRelativeBaseName, locale)));
    }

    if (resource != null) {
      log.debug("Found local bundle {}", classRelativeBaseName);
      ResourceBundle localBundle = ResourceBundle.getBundle(classRelativeBaseName, locale, control);
      return new ResourceBundleWrapper(fileName, localBundle, bundle.getBundle(), classRelativeBaseName + "_" + locale.getLanguage() + ".properties", locale);
    } else {
      return bundle;
    }
  }

  /**
   * Use this method to get a translation for a key.
   * The key "hello.world" is stored like that:
   * hello.world=Hello {0}{1}
   * And the corresponding method parameters will be:
   * "hello.world", "world", "!"
   * Which will result in:
   * Hello world!
   * If you add a colon ":" to the end of the string it is ignored.
   * This is quite useful for input fields.
   *
   * @param key
   * @param args
   * @return
   */
  public String get(String key, Object... args) {
    String string = getBundle().getString(key);
    if (args == null) {
      return string;
    } else {
      for (int i = 0; i < args.length; i++) {
        Object arg = args[i];
        if (arg == null) {
          string = string.replace("{" + i + "}", "null");
        } else {
          string = string.replace("{" + i + "}", arg.toString());
        }
      }
      return string;
    }
  }

  public String get(Field field) {
    String key = field.getDeclaringClass().getName();
    key += field.getName();
    return get(key);
  }
}
