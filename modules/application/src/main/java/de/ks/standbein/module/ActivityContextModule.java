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
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;
import de.ks.standbein.activity.context.ActivityContext;
import de.ks.standbein.activity.context.ActivityContextService;
import de.ks.standbein.activity.context.ActivityScoped;
import de.ks.standbein.launch.Service;

public class ActivityContextModule extends AbstractModule {
  public static final String EXECUTOR_COREPOOLSIZE = "ActivityExecutor.corepoolsize";
  public static final String EXECUTOR_MAXPOOLSIZE = "ActivityExecutor.maxpoolsize";

  @Override
  protected void configure() {
    ActivityContext context = new ActivityContext();

    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(ActivityContextService.class);

    bindScope(ActivityScoped.class, context);
    bind(ActivityContext.class).toInstance(context);


    OptionalBinder.newOptionalBinder(binder(), Key.get(Integer.class, Names.named(EXECUTOR_COREPOOLSIZE)))//
      .setDefault().toInstance(8);
    OptionalBinder.newOptionalBinder(binder(), Key.get(Integer.class, Names.named(EXECUTOR_MAXPOOLSIZE)))//
      .setDefault().toInstance(Integer.MAX_VALUE);
  }
}
