package de.ks.eventsystem.bus;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import de.ks.reflection.ReflectionUtil;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 *
 */
class EventHandler {
  private static final Logger log = LogManager.getLogger(EventHandler.class);
  protected final WeakReference<Object> target;
  protected final Method method;
  protected final Integer priority;
  protected final HandlingThread handlingThread;

  protected EventHandler(Object target, Method method) {
    this.target = new WeakReference<>(target);
    this.method = method;
    if (method.isAnnotationPresent(Priority.class)) {
      Priority annotation = method.getAnnotation(Priority.class);
      priority = annotation.value();
    } else {
      priority = Integer.MAX_VALUE;
    }
    if (method.isAnnotationPresent(Threading.class)) {
      handlingThread = method.getAnnotation(Threading.class).value();
    } else {
      handlingThread = HandlingThread.Sync;
    }
  }

  public boolean isValid() {
    return target.get() != null;
  }

  public boolean handleEvent(Object event, boolean wait) {
    if (isValid()) {
      Object targetInstance = target.get();
      Object retval = null;
      switch (this.handlingThread) {
        case Sync:
          retval = ReflectionUtil.invokeMethod(method, targetInstance, event);
          break;
        case Async:
          executeAsync(event, targetInstance, wait);
          break;
        case JavaFX:
          retval = handleInJavaFXThread(event, targetInstance, wait);
          break;
      }

      if (retval instanceof Boolean) {
        return (Boolean) retval;
      } else if (retval != null && retval.getClass().isPrimitive() && Boolean.TYPE.equals(retval.getClass())) {
        return (boolean) retval;
      }
    }
    return false;
  }

  protected void executeAsync(Object event, Object targetInstance, boolean wait) {
    Future<?> future = ForkJoinPool.commonPool().submit((Runnable) () -> ReflectionUtil.invokeMethod(method, targetInstance, event));
    if (wait) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        log.error("Could not execute event asynchronously ", e);
      }
    }
  }

  protected Object handleInJavaFXThread(Object event, Object targetInstance, boolean wait) {
    Object retval = null;
    if (Platform.isFxApplicationThread()) {
      retval = ReflectionUtil.invokeMethod(method, targetInstance, event);
    } else {
      FutureTask<Class<Void>> task = new FutureTask<>(() -> ReflectionUtil.invokeMethod(method, targetInstance, event), Void.class);
      Platform.runLater(task);
      if (wait) {
        try {
          task.get();
        } catch (InterruptedException | ExecutionException e) {
          log.error("Could not execute event in JavaFX thread", e);
        }
      }
    }
    return retval;
  }
}