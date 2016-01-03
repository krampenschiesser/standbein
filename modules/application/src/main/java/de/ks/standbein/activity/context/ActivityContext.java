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

package de.ks.standbein.activity.context;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 */
public class ActivityContext implements Scope {
  private static final Logger log = LoggerFactory.getLogger(ActivityContext.class);
  public static final String INITIAL_ACTIVITY = "Initial-Unbound";

  protected final Objenesis objenesis = new ObjenesisStd(true);
  protected final ConcurrentHashMap<String, ActivityHolder> activities = new ConcurrentHashMap<>();
  protected final ConcurrentHashMap<Key<?>, Object> proxies = new ConcurrentHashMap<>();

  protected volatile String currentActivity = null;
  protected volatile Injector injector;

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

  public ActivityContext() {
    startActivity(INITIAL_ACTIVITY);
  }

  public void start(String id) {
    if (!INITIAL_ACTIVITY.equals(id)) {
      startActivity(id);
    }
  }

  public void stop(String id) {
    if (!INITIAL_ACTIVITY.equals(id)) {
      stopActivity(id);
    }
  }

  public void cleanup(String id) {
    if (!INITIAL_ACTIVITY.equals(id)) {
      cleanupSingleActivity(id);
    }
  }

  public void stopAll() {
    cleanupAllActivities();
  }

  protected void cleanupSingleActivity(String id) {
    lock.writeLock().lock();
    try {
      ActivityHolder activityHolder = activities.remove(id);
      log.debug("Cleanup activity {}", activityHolder.getId());
      activityHolder.destroy();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void cleanupSingleBean(Key<?> key) {
    lock.writeLock().lock();
    try {
      ActivityHolder activityHolder = activities.get(getCurrentActivity());

      Object instance = activityHolder.objectStore.remove(key);
      if (instance != null) {
        log.debug("Cleaned up bean {} of activity {}", key, activityHolder.getId());
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected ActivityHolder startActivity(String id) {
    lock.writeLock().lock();
    try {
      currentActivity = id;
      if (activities.containsKey(id)) {
        log.debug("Resuming activity {}", id);
        return activities.get(id);
      } else {
        ActivityHolder holder = new ActivityHolder(id);
        this.activities.put(id, holder);

        log.debug("Started activity {}", holder.getId());
        return holder;
      }

    } finally {
      lock.writeLock().unlock();
    }
  }

  public void stopActivity(String id) {
    lock.writeLock().lock();
    try {
      ActivityHolder activityHolder = activities.get(id);
      if (activityHolder == null) {
        log.warn("Activity {} is already stopped", id);
        return;
      }
      int count = activityHolder.getCount().decrementAndGet();
      if (count == 0) {
        cleanupSingleActivity(id);
        currentActivity = null;
        log.debug("Stopped activity {}", activityHolder.getId());
      } else {
        log.debug("Don't stop activity {} because of {} holders.", activityHolder.getId(), count);
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected void cleanupAllActivities() {
    log.debug("Cleanup all activities.");

    lock.writeLock().lock();
    try {
      HashSet<String> removed = new HashSet<>();
      for (String id : activities.keySet()) {
        if (INITIAL_ACTIVITY.equals(id)) {
          continue;
        }
        ActivityHolder activityHolder = activities.get(id);
        if (activityHolder == null) {
          log.error("No activity active in thread {}", Thread.currentThread().getName());
          throw new IllegalStateException("No activity active in thread " + Thread.currentThread().getName());
        }
        if (multipleThreadsActive(activityHolder)) {
          log.warn("There are still {} other threads holding a reference to this activity, cleanup not allowed", activityHolder.getCount().get() - 1);
        }
        waitForOtherThreads(activityHolder);
        cleanupSingleActivity(id);
        removed.add(id);
      }
      this.activities.keySet().removeAll(removed);
    } finally {
      lock.writeLock().unlock();
    }
  }

  private void waitForOtherThreads(ActivityHolder activityHolder) {
    while (multipleThreadsActive(activityHolder)) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        log.error("Interrupted", e);
      }
    }
  }

  private boolean multipleThreadsActive(ActivityHolder activityHolder) {
    return activityHolder.getCount().get() > 1;
  }

  public ActivityHolder getHolder() {
    if (currentActivity == null) {
      throw new RuntimeException("No activity active in current thread!");
    }
    return activities.get(currentActivity);
  }

  public String getCurrentActivity() {
    return currentActivity;
  }

  public boolean hasCurrentActivity() {
    return currentActivity != null;
  }

  @Override
  public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
    return new Provider<T>() {
      @Override
      public T get() {
        Object instance = getOrCreateInstance(key, unscoped);
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) instance.getClass();
        return getProxy(clazz, key);
      }

      @Override
      public String toString() {
        return ActivityContext.class.getSimpleName() + "-Provider. Backed by: " + unscoped;
      }
    };
  }

  private Object getOrCreateInstance(Key<?> key, Provider<?> unscoped) {
    ActivityHolder holder = activities.get(currentActivity);
    Object instance = null;
    lock.readLock().lock();
    try {
      instance = holder.getStoredInstance(key);
    } finally {
      lock.readLock().unlock();
    }

    if (instance == null) {
      lock.writeLock().lock();
      try {
        instance = unscoped.get();
        holder.put(key, instance);
        log.debug("For {} created new instance {} and put it to store", key, instance);
      } finally {
        lock.writeLock().unlock();
      }
    } else {
      log.trace("For {} found instance {}", key, instance);
    }

    return instance;
  }

  private Object getCurrentInstance(Key<?> key) {
    ActivityHolder holder = activities.get(currentActivity);
    Object storedBean = holder.getStoredInstance(key);
    if (storedBean == null) {
      if (injector == null) {
        throw new IllegalStateException("Injector not yet set! Trying to get " + key);
      }
      log.debug("Try to load {} from proxy, but found none for current activity.", key);
      return injector.getProvider(key).get();
    } else {
      log.trace("For {} from proxy, loaded {}.", key, storedBean);
      return storedBean;
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T getProxy(Class<T> clazz, Key<T> key) {
    if (proxies.containsKey(key)) {
      return (T) proxies.get(key);
    } else {
      ProxyFactory factory = new ProxyFactory();
      factory.setSuperclass(clazz);
      Class proxy = factory.createClass();

      Object retval = objenesis.newInstance(proxy);
      ((Proxy) retval).setHandler(new MethodHandler() {
        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
          Object currentInstance = getCurrentInstance(key);
          if (thisMethod.getName().equals("toString")) {
            return "Proxy for " + clazz.getName() + " current activity: '" + currentActivity + "'" + String.valueOf(thisMethod.invoke(currentInstance, args));
          }
          return thisMethod.invoke(currentInstance, args);
        }
      });
      proxies.putIfAbsent(key, retval);
      return (T) retval;
    }
  }

  public Set<String> getActivities() {
    return Collections.unmodifiableSet(activities.keySet());
  }

  public boolean isMainActivity(String activity) {
    return activities.keySet().size() == 2 && activities.containsKey(activity) && activities.containsKey(INITIAL_ACTIVITY);
  }

  public void setInjector(Injector injector) {
    this.injector = injector;
  }
}
