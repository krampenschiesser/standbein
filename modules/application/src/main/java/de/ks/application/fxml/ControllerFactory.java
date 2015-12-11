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

package de.ks.application.fxml;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import de.ks.activity.context.ActivityContext;
import de.ks.activity.initialization.ActivityInitialization;
import de.ks.reflection.ReflectionUtil;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ControllerFactory implements Callback<Class<?>, Object> {
  private static final Logger log = LoggerFactory.getLogger(ControllerFactory.class);

  private final Injector injector;
  private final ActivityContext context;
  private final ActivityInitialization initialization;

  @Inject
  public ControllerFactory(Injector injector, ActivityContext context, ActivityInitialization initialization) {
    this.injector = injector;
    this.context = context;
    this.initialization = initialization;
  }

  @Override
  public Object call(Class<?> clazz) {
    checkNoScopeBinding(clazz);

    try {
      Object instance = injector.getInstance(clazz);
      registerLoadedController(instance);
      return instance;
    } catch (ConfigurationException e) {
      return constructUnjectable(clazz);
    }
  }

  private void checkNoScopeBinding(Class<?> clazz) {
    Set<Class<? extends Annotation>> scopes = injector.getScopeBindings().keySet();
    for (Annotation annotation : clazz.getAnnotations()) {
      if (scopes.contains(annotation.annotationType())) {
        throw new IllegalStateException("Class " + clazz.getName() + " is not allowed to be in scope " + annotation + " because JavaFX can't inject fields in proxy types");
      }
    }
  }

  private Object constructUnjectable(Class<?> clazz) {
    List<Field> injectedFields = ReflectionUtil.getAllFields(clazz, (f) -> f.isAnnotationPresent(Inject.class));
    if (!injectedFields.isEmpty()) {
      throw new IllegalArgumentException("Unable to instanitate class " + clazz.getName() + " that defines injected fields but is no bean.");
    } else {
      Object newInstance = ReflectionUtil.newInstance(clazz, false);
      registerLoadedController(newInstance);
      return newInstance;
    }
  }

  protected void registerLoadedController(Object object) {
    if (context.hasCurrentActivity()) {
      initialization.addControllerToInitialize(object);
    }
  }
}
