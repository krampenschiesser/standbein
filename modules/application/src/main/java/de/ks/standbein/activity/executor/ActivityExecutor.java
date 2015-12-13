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
package de.ks.standbein.activity.executor;

import de.ks.executor.CancelRejection;
import de.ks.standbein.activity.context.ActivityContext;
import de.ks.standbein.activity.context.ActivityScoped;
import de.ks.standbein.module.ActivityContextModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@ActivityScoped
public class ActivityExecutor implements ScheduledExecutorService {
  private static final Logger log = LoggerFactory.getLogger(ActivityExecutor.class);
  private final ScheduledThreadPoolExecutor delegate;

  private final AtomicInteger threadCount = new AtomicInteger();
  private final Provider<ActivityContext> context;

  @Inject
  public ActivityExecutor(Provider<ActivityContext> context, @Named(ActivityContextModule.EXECUTOR_COREPOOLSIZE) int corePoolSize, @Named(ActivityContextModule.EXECUTOR_MAXPOOLSIZE) int maximumPoolSize) {
    this.context = context;
    delegate = new ScheduledThreadPoolExecutor(corePoolSize, new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        int id = threadCount.incrementAndGet();
        thread.setName(context.get().getCurrentActivity() + "-" + String.format("%02d", id));
        thread.setDaemon(true);
        return thread;
      }
    });
    delegate.setMaximumPoolSize(maximumPoolSize);
    delegate.setKeepAliveTime(1, TimeUnit.MINUTES);
    delegate.setRejectedExecutionHandler(new CancelRejection());
  }

  public String getName() {
    return context.get().getCurrentActivity();
  }

  public void waitForAllTasksDone() {
    while (!delegate.isShutdown() && delegate.getActiveCount() > 0) {
      try {
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (InterruptedException e) {
        log.trace("Got interrupted while waiting for tasks.", e);
      }
    }
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return delegate.schedule(command, delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    return delegate.schedule(callable, delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    return delegate.scheduleAtFixedRate(command, initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    return delegate.scheduleWithFixedDelay(command, initialDelay, delay, unit);
  }

  @Override
  public void execute(Runnable command) {
    delegate.execute(command);
  }

  @Override
  public Future<?> submit(Runnable task) {
    return delegate.submit(task);
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return delegate.submit(task, result);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return delegate.submit(task);
  }

  @Override
  public void shutdown() {
    delegate.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return delegate.shutdownNow();
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    return delegate.invokeAny(tasks);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return delegate.invokeAny(tasks, timeout, unit);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
    return delegate.invokeAll(tasks);
  }

  public void setCorePoolSize(int coreSize) {
    delegate.setCorePoolSize(coreSize);
  }

  public void setMaxPoolSize(int maxSize) {
    delegate.setMaximumPoolSize(maxSize);
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
    return delegate.invokeAll(tasks, timeout, unit);
  }

  @Override
  public boolean isShutdown() {
    return delegate.isShutdown();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return delegate.awaitTermination(timeout, unit);
  }

  public boolean isTerminating() {
    return delegate.isTerminating();
  }

  @Override
  public boolean isTerminated() {
    return delegate.isTerminated();
  }

  public ScheduledThreadPoolExecutor getDelegate() {
    return delegate;
  }
}
