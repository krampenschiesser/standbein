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

import de.ks.IntegrationTestModule;
import de.ks.LoggingGuiceTestSupport;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

public class ScopeTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule()).launchServices();

  @Inject
  ActivityScopedBean1 bean1;
  @Inject
  ActivityScopedBean1 bean2;
  @Inject
  ActivityContext context;
  @Inject
  ExecutorService service;
  @After
  public void tearDown() throws Exception {
    context.stopAll();
  }

  @Test
  public void testActivityContextActive() throws Exception {
    context.startActivity("1").getCount();

    bean1.getName();

    context.startActivity("2");
    bean2.getName();
    assertEquals(3, context.activities.size());


    context.stopActivity("1");
    assertEquals(2, context.activities.size());
    assertNull(context.activities.get("1"));
    assertFalse(context.activities.get("2").getObjectStore().isEmpty());

    context.cleanupAllActivities();
    assertEquals(1, context.activities.size());
  }

  @Inject
  Provider<ActivityScopedBean1> provider;

  @Test
  public void testScopePropagation() throws Exception {
    context.start("2");

    bean1.setValue("Hello Sauerland!");

    Callable<Object> callable = () -> {
      ActivityScopedBean1 bean = provider.get();
      assertNotNull(bean.getValue());
      assertEquals("Hello Sauerland!", bean.getValue());
      return null;
    };
    service.invokeAny(Collections.singletonList(callable));
    assertEquals("Hello Sauerland!", bean1.getValue());
  }

  @Ignore
  @Test
  public void testScopeCleanup() throws Exception {
    final CyclicBarrier barrier = new CyclicBarrier(2);
    context.start("2");

    bean1.setValue("Hello Sauerland!");

    service.execute(() -> {
      ActivityScopedBean1 bean = provider.get();
      assertNotNull(bean.getValue());
      assertEquals("Hello Sauerland!", bean.getValue());
      try {
        barrier.await();
      } catch (InterruptedException | BrokenBarrierException e) {
        //brz
      }
    });
    assertEquals(1, context.activities.size());
    assertEquals("Hello Sauerland!", bean1.getValue());
    context.stop("2");

    assertEquals("Activity got removed although one thread still holds it", 1, context.activities.size());
    barrier.await();
    Thread.sleep(100);

    assertEquals(0, context.activities.size());
  }
}
