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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import de.ks.eventsystem.EventBusModule;
import de.ks.standbein.i18n.LocalizationModule;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApplicationModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new ActivityContextModule());
    install(new ApplicationServiceModule());
    install(new LocalizationModule());
    install(new EventBusModule());
  }

  @Provides
  @Singleton
  public ExecutorService getExecutorService() {
    return Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("application-common-%d").build());
  }
}
