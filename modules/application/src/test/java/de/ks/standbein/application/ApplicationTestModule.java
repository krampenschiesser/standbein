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
package de.ks.standbein.application;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import de.ks.eventsystem.EventBusModule;
import de.ks.standbein.activity.InitialActivity;
import de.ks.standbein.activity.resource.ResourceActivity;
import de.ks.standbein.i18n.LocalizationModule;
import de.ks.standbein.imagecache.ImageModule;
import de.ks.standbein.module.ActivityContextModule;
import de.ks.standbein.module.ApplicationServiceModule;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApplicationTestModule extends AbstractModule {

  public static final String APP_TITLE = "Hello Sauerland";
  public static final int APP_SIZE = 400;

  protected boolean useMainWindow;
  protected boolean useInitialActivity;

  public ApplicationTestModule useInitialActivity() {
    useInitialActivity = true;
    useMainWindow = false;
    return this;
  }

  public ApplicationTestModule useMainWindow() {
    useMainWindow = true;
    useInitialActivity = false;
    return this;
  }

  @Override
  protected void configure() {
    bind(boolean.class).annotatedWith(Names.named(ApplicationServiceModule.PREVENT_PLATFORMEXIT)).toInstance(true);

    ApplicationCfg applicationCfg = new ApplicationCfg("apptitle", APP_SIZE, APP_SIZE).setIcon("keymap.jpg");
    bind(ApplicationCfg.class).toInstance(applicationCfg);

    if (useInitialActivity) {
      bind(InitialActivity.class).toInstance(new InitialActivity(ResourceActivity.class));
    } else if (useMainWindow) {
      bind(MainWindow.class).to(MainTestWindow.class);
    }

    install(new ActivityContextModule());
    install(new ApplicationServiceModule());
    install(new LocalizationModule());
    install(new EventBusModule());
    install(new ImageModule());
  }

  @Provides
  @Singleton
  public ExecutorService getExecutorService() {
    return Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("application-common-%d").build());
  }
}
