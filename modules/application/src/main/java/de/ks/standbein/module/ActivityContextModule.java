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
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import de.ks.standbein.activity.context.ActivityContext;
import de.ks.standbein.activity.context.ActivityContextService;
import de.ks.standbein.activity.context.ActivityScoped;
import de.ks.standbein.activity.executor.ActivityExecutor;
import de.ks.standbein.launch.Service;

public class ActivityContextModule extends AbstractModule {
  @Override
  protected void configure() {
    ActivityContext context = new ActivityContext();

    Multibinder.newSetBinder(binder(), Service.class).addBinding().to(ActivityContextService.class);

    bindScope(ActivityScoped.class, context);
    bind(ActivityContext.class).toInstance(context);

    bind(Integer.class).annotatedWith(Names.named(ActivityExecutor.EXECUTOR_COREPOOLSIZE)).toInstance(8);
    bind(Integer.class).annotatedWith(Names.named(ActivityExecutor.EXECUTOR_MAXPOOLSIZE)).toInstance(Integer.MAX_VALUE);
  }
}
