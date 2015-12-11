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
package de.ks.standbein;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;

public class GuiceSupport {
  public static final GuiceSupport instance = new GuiceSupport();

  private final AtomicReference<Injector> injectorRef = new AtomicReference<>();

  /**
   * Loads using service discovery
   */
  public void load() {
    load(discoverModules());
  }

  public Collection<Module> discoverModules() {
    ArrayList<Module> modules = new ArrayList<>();
    ServiceLoader<Module> loader = ServiceLoader.load(Module.class);
    for (Module module : loader) {
      modules.add(module);
    }
    return modules;
  }

  public synchronized void load(Collection<Module> modules) {
    Injector injector = Guice.createInjector(modules);
    injectorRef.compareAndSet(null, injector);
  }

  public void reset() {
    reset(null);
  }

  public void reset(@Nullable Collection<Module> modules) {
    if (modules == null || modules.isEmpty()) {
      injectorRef.set(null);
    } else {
      Injector injector = Guice.createInjector(modules);
      injectorRef.set(injector);
    }
  }

  public Injector getInjector() {
    if (injectorRef.get() == null) {
      load();
    }
    return injectorRef.get();
  }

  public static <E> E get(Class<E> clazz) {
    return instance.getInjector().getInstance(clazz);
  }
}
