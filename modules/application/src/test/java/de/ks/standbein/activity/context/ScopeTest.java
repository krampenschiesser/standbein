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

import de.ks.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.concurrent.Callable;
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
    context.start("1");

    bean1.getName();

    context.startActivity("2");
    bean2.getName();
    assertEquals(3, context.activities.size());


    context.stop("1");
    assertEquals(2, context.activities.size());
    assertNull(context.activities.get("1"));
    assertFalse(context.activities.get("2").getObjectStore().isEmpty());

    context.stopAll();
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
}
