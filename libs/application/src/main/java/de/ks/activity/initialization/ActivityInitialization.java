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
package de.ks.activity.initialization;

import de.ks.activity.ActivityCfg;
import de.ks.activity.ActivityController;
import de.ks.activity.context.ActivityScoped;
import de.ks.application.fxml.DefaultLoader;
import de.ks.eventsystem.bus.EventBus;
import de.ks.executor.JavaFXExecutorService;
import javafx.fxml.LoadException;
import javafx.scene.Node;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ActivityScoped
public class ActivityInitialization {
  private static final Logger log = LoggerFactory.getLogger(ActivityInitialization.class);

  protected final ConcurrentHashMap<Class<?>, Pair<Object, Node>> controllers = new ConcurrentHashMap<>();
  protected final Map<Class<?>, CompletableFuture<DefaultLoader<Node, Object>>> preloads = new HashMap<>();
  protected final ThreadLocal<List<Object>> currentlyLoadedControllers = ThreadLocal.withInitial(ArrayList::new);
  protected final List<DatasourceCallback> dataStoreCallbacks = new ArrayList<>();
  protected final List<ActivityCallback> activityCallbacks = new ArrayList<>();

  @Inject
  ActivityController controller;

  public void loadActivity(ActivityCfg activityCfg) {
    currentlyLoadedControllers.get().clear();
    loadControllers(activityCfg);
    initalizeControllers();
  }

  protected void loadControllers(ActivityCfg activityCfg) {
    loadController(activityCfg.getInitialController());
    activityCfg.getAdditionalControllers().forEach(c -> loadController(c));
    preloads.values().forEach(l -> l.join());
  }

  private boolean shouldLoadInFXThread(Class<?> clazz) {
    return clazz.isAnnotationPresent(LoadInFXThread.class);
  }

  @SuppressWarnings("unchecked")
  public <T> CompletableFuture<DefaultLoader<Node, T>> loadAdditionalController(Class<T> controllerClass) {
    DefaultLoader<Node, Object> loader = new DefaultLoader<>(controllerClass);

    if (shouldLoadInFXThread(controllerClass)) {
      loader = controller.getJavaFXExecutor().invokeInJavaFXThread(loader::load);
      log.debug("Loaded additional controller {} in fx thread", controllerClass);
    } else {
      loader.load();
      log.info("Loaded additional controller {} in current thread", controllerClass);
    }
    currentlyLoadedControllers.get().add(loader.getController());
    CompletableFuture completed = CompletableFuture.completedFuture(loader);
    return completed;
  }

  private void loadController(Class<?> controllerClass) {
    JavaFXExecutorService javaFXExecutor = controller.getJavaFXExecutor();
    ExecutorService executorService;
    if (shouldLoadInFXThread(controllerClass)) {
      executorService = javaFXExecutor;
    } else {
      executorService = controller.getExecutorService();
    }

    if (!preloads.containsKey(controllerClass)) {
      CompletableFuture<DefaultLoader<Node, Object>> loaderFuture = CompletableFuture.supplyAsync(getDefaultLoaderSupplier(controllerClass), executorService).exceptionally((t) -> {
        if (t.getCause() instanceof RuntimeException && t.getCause().getCause() instanceof LoadException) {
          EventBus eventBus = CDI.current().select(EventBus.class).get();
          currentlyLoadedControllers.get().forEach(eventBus::unregister);
          currentlyLoadedControllers.get().clear();
          log.info("Last load of {} failed, will try again in JavaFX Thread", new DefaultLoader<>(controllerClass).getFxmlFile());
          return javaFXExecutor.invokeInJavaFXThread(() -> getDefaultLoaderSupplier(controllerClass).get());
        }
        throw new RuntimeException(t);
      });

      preloads.put(controllerClass, loaderFuture);
    }
  }

  private Supplier<DefaultLoader<Node, Object>> getDefaultLoaderSupplier(Class<?> controllerClass) {
    return () -> {
      DefaultLoader<Node, Object> loader = new DefaultLoader<>(controllerClass);
      loader.load();
      Node view = loader.getView();

      currentlyLoadedControllers.get().forEach((c) -> {
        assert c != null;
        log.debug("Registering controller {} with node {}", c, view);
        controllers.put(c.getClass(), Pair.of(c, view));
      });
      currentlyLoadedControllers.get().clear();
      return loader;
    };
  }

  public void addControllerToInitialize(Object controller) {
    currentlyLoadedControllers.get().add(controller);
  }

  public void initalizeControllers() {
    dataStoreCallbacks.clear();
    dataStoreCallbacks.addAll(controllers.values().stream().map(p -> p.getLeft()).filter(o -> o instanceof DatasourceCallback).map(o -> (DatasourceCallback) o).collect(Collectors.toList()));
    activityCallbacks.clear();
    activityCallbacks.addAll(controllers.values().stream().map(p -> p.getLeft()).filter(o -> o instanceof ActivityCallback).map(o -> (ActivityCallback) o).collect(Collectors.toList()));
    Collections.sort(dataStoreCallbacks);
  }

  public Node getViewForController(Class<?> targetController) {
    if (!controllers.containsKey(targetController)) {
      throw new IllegalArgumentException("Controller " + targetController.getName() + " is not registered. Registered are " + controllers.keySet());
    }
    return controllers.get(targetController).getRight();
  }

  @SuppressWarnings("unchecked")
  public <T> T getControllerInstance(Class<T> targetController) {
    if (!controllers.containsKey(targetController)) {
      throw new IllegalArgumentException("Controller " + targetController + " is not registered. Registered are " + controllers.keySet());
    }
    return (T) controllers.get(targetController).getLeft();
  }

  public Collection<Object> getControllers() {
    return controllers.values().stream().map(pair -> pair.getKey()).collect(Collectors.toList());
  }

  public List<DatasourceCallback> getDataStoreCallbacks() {
    return dataStoreCallbacks;
  }

  public List<ActivityCallback> getActivityCallbacks() {
    return activityCallbacks;
  }
}
