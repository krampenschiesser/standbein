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
package de.ks.standbein.module;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.Multibinder;
import de.ks.standbein.application.ApplicationService;
import de.ks.standbein.javafx.FxCss;
import de.ks.standbein.launch.Service;
import de.ks.standbein.validation.DefaultDecorator;

public class ApplicationServiceModule extends AbstractModule {
  public static final String WAIT_FOR_INITIALIZATION = "waitForInitialization";
  public static final String PREVENT_PLATFORMEXIT = "preventPlatformExit";
  public static final String APPICON = "iconName";

  @Override
  protected void configure() {
    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(ApplicationService.class);
    Multibinder.newSetBinder(binder(), Key.get(String.class, FxCss.class)).addBinding().toInstance(DefaultDecorator.CSS_FILE_PATH);
  }
}
