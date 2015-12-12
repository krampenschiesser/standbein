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

package de.ks.activity.context;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 */
public class ActivityContext implements Scope {
  private static final Logger log = LoggerFactory.getLogger(ActivityContext.class);

  protected final ConcurrentHashMap<String, ActivityHolder> activities = new ConcurrentHashMap<>();
  protected volatile String currentActivity = null;

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

  public void cleanupSingleActivity(String id) {
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

      StoredBean storedBean = activityHolder.objectStore.remove(key);
      if (storedBean != null) {
        log.debug("Cleaned up bean {} of activity {}", key, activityHolder.getId());
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public ActivityHolder startActivity(String id) {
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

  public void cleanupAllActivities() {
    log.debug("Cleanup all activities.");
    for (String id : activities.keySet()) {
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
    }
    lock.writeLock().lock();
    try {
      this.activities.clear();
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

        ActivityHolder holder = activities.get(currentActivity);
        StoredBean storedBean = null;
        lock.readLock().lock();
        try {
          storedBean = holder.getStoredBean(key);
        } finally {
          lock.readLock().unlock();
        }

        if (storedBean == null) {
          try {
            lock.writeLock().lock();
            storedBean = new StoredBean(key, createProxy(unscoped.get()));
            holder.put(key, storedBean);
          } finally {
            lock.readLock().unlock();
          }
        }
        return null;
      }
    };
  }

  private <T> StoredBean createProxy(T t) {
    return null;
  }

  public void stopAll() {
    cleanupAllActivities();
  }
}
