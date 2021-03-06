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
package de.ks.standbein.launch;

import com.google.inject.Inject;
import de.ks.standbein.preload.LaunchListener;
import de.ks.standbein.preload.LaunchListenerAdapter;
import de.ks.standbein.preload.PreloaderApplication;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Singleton
public class Launcher {
  private static final Logger log = LoggerFactory.getLogger(Launcher.class);

  private final List<Service> services;
  private final ExecutorService executorService;

  private final List<Throwable> startupExceptions = Collections.synchronizedList(new ArrayList<>());

  private volatile CountDownLatch latch;
  private volatile LaunchListener launchListener = new LaunchListenerAdapter();
  private Class<? extends PreloaderApplication> preloader;
  private Future<?> preloaderFuture;

  private volatile PreloaderApplication preloaderInstance;
  private CountDownLatch preloaderLatch = new CountDownLatch(1);

  public static volatile Launcher instanceForFx;

  @Inject
  public Launcher(Set<Service> services, ExecutorService executorService) {
    this.services = new ArrayList<>(services);
    this.executorService = executorService;
    this.services.sort((o1, o2) -> Integer.compare(o1.getRunLevel(), o2.getRunLevel()));
  }

  public <S extends Service> void removeService(Class<S> clazz) {
    S service = getService(clazz);
    services.remove(service);
  }

  @SuppressWarnings("unchecked")
  public <S extends Service> S getService(Class<S> clazz) {
    List<Service> collect = services.stream().filter((s) -> s.getClass().equals(clazz)).collect(Collectors.toList());
    if (collect.isEmpty()) {
      return null;
    } else {
      return (S) collect.get(0);
    }
  }

  @SuppressWarnings("unchecked")
  public <S extends Service> S getService(String name) {
    List<Service> collect = services.stream().filter((s) -> s.getName().equals(name)).collect(Collectors.toList());
    if (collect.isEmpty()) {
      return null;
    } else {
      return (S) collect.get(0);
    }
  }

  public TreeMap<Integer, List<Service>> getServiceWaves() {
    TreeMap<Integer, List<Service>> retval = new TreeMap<>();
    services.forEach((service) -> {
      int runlevel = service.getRunLevel();
      retval.putIfAbsent(runlevel, new ArrayList<>());
      retval.get(runlevel).add(service);
    });
    return retval;
  }

  public void startAllAndWait(String... args) {
    startAll(args);
    awaitStart();
  }

  public void startAll(String... args) {
    if (preloader != null) {
      instanceForFx = this;
      startPreloader();
    }
    TreeMap<Integer, List<Service>> waves = getServiceWaves();
    launchListener.totalWaves(waves.keySet().size());
    launchListener.wavePriorities(waves.keySet());
    latch = new CountDownLatch(waves.keySet().size());
    Iterator<Integer> iter = waves.keySet().iterator();
    startWave(iter, waves, args);
    log.info("Launching done!");
  }

  private void startWave(Iterator<Integer> iter, TreeMap<Integer, List<Service>> waves, String[] args) {
    if (!iter.hasNext()) {
      return;
    }
    Integer runlevel = iter.next();
    launchListener.waveStarted(runlevel);
    log.info("Starting services with runlevel {}", runlevel);
    List<CompletableFuture<Void>> waveFutures = waves.get(runlevel).stream()//
      .map((s) -> {
        return CompletableFuture.supplyAsync(() -> {
          s.initialize(this, executorService, args);
          return s.start();
        }, executorService)//
          .thenAccept((service) -> log.info("Successfully started service {}", service.getName()));
      }).collect(Collectors.toList());

    CompletableFuture<Void> allOf = CompletableFuture.allOf(waveFutures.toArray(new CompletableFuture[waveFutures.size()]));
    allOf.thenRun(() -> log.info("Started services with runlevel {}", runlevel))//
      .thenRun(() -> latch.countDown())//
      .thenRun(() -> launchListener.waveFinished(runlevel))//
      .thenRun(() -> startWave(iter, waves, args))//
      .exceptionally((t) -> {
        while (latch.getCount() > 0) {
          latch.countDown();
        }
        startupExceptions.add(t);
        launchListener.failure(t.toString(), t);
        //throw new RuntimeException(t);
        return null;
      });
  }

  public void awaitStart() {
    try {
      latch.await();
      if (!startupExceptions.isEmpty()) {
        RuntimeException runtimeException = new RuntimeException("Startup failed");
        startupExceptions.forEach((t) -> {
          log.error("Failed startup.", t);
          runtimeException.addSuppressed(t);
        });
        throw runtimeException;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean isStarted() {
    for (Service service : services) {
      if (!service.isRunning()) {
        return false;
      }
    }
    return true;
  }

  public void stopAllAndWait() {
    stopAll();
    awaitStop();
  }

  public void stopAll() {
    try {
      latch.await();
    } catch (InterruptedException e) {
      log.error("Could not await latch.", e);
    }
    TreeMap<Integer, List<Service>> waves = getServiceWaves();
    latch = new CountDownLatch(waves.keySet().size());
    Iterator<Integer> iter = waves.descendingKeySet().iterator();
    stopWave(iter, waves);
  }

  private void stopWave(Iterator<Integer> iter, TreeMap<Integer, List<Service>> waves) {
    if (!iter.hasNext()) {
      return;
    }
    Integer runlevel = iter.next();
    log.info("Stopping services with runlevel {}", runlevel);
    List<CompletableFuture<Void>> waveFutures = waves.get(runlevel).stream()//
      .map((s) -> {
        if (s.isStopped()) {
          return CompletableFuture.<Void>completedFuture(null);
        } else {
          return CompletableFuture.supplyAsync(() -> s.stop(), executorService)//
            .thenAccept((service) -> log.info("Successfully stopped service {}", service.getName()));
        }
      }).collect(Collectors.toList());

    CompletableFuture<Void> allOf = CompletableFuture.allOf(waveFutures.toArray(new CompletableFuture[waveFutures.size()]));
    allOf.thenRun(() -> log.info("Stopped services with runlevel {}", runlevel))//
      .thenRun(() -> latch.countDown())//
      .thenRun(() -> stopWave(iter, waves))//
      .exceptionally((t) -> {
        log.info("Failed to stop services", t);
        return null;
      });
  }

  public void awaitStop() {
    if (latch != null) {
      try {
        latch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    waitForPreloader();
  }

  public void waitForPreloader() {
    if (preloaderFuture != null) {
      try {
        preloaderFuture.get();
      } catch (InterruptedException e) {
        //ok
      } catch (ExecutionException e) {
        log.error("Error from preloader ", e);
      }
    }
  }

  public void startPreloader() {
    preloaderFuture = executorService.submit(() -> Application.launch(preloader));
    try {
      preloaderLatch.await();
    } catch (InterruptedException e) {
      //
    }
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  public void setPreloader(Class<? extends PreloaderApplication> preloader) {
    this.preloader = preloader;
  }

  public Class<? extends PreloaderApplication> getPreloader() {
    return preloader;
  }

  public void setLaunchListener(LaunchListener launchListener) {
    this.launchListener = launchListener;
  }

  public LaunchListener getLaunchListener() {
    return launchListener;
  }

  public void setPreloaderInstance(PreloaderApplication preloaderInstance) {
    this.preloaderInstance = preloaderInstance;
    instanceForFx = null;
    preloaderLatch.countDown();
  }

  public PreloaderApplication getPreloaderInstance() {
    return preloaderInstance;
  }

  public List<Service> getServices() {
    return services;
  }

  public void waitForUIThreads() {
    services.stream().filter(s -> s instanceof UIService).map(e -> (UIService) e).forEach(UIService::waitForUIThread);
  }

  public void launchAndWaitForUIThreads(String... args) {
    startAllAndWait(args);
    try {
      waitForUIThreads();
    } finally {
      stopAllAndWait();
    }
  }
}
