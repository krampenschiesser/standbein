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
package de.ks.standbein.activity.datasource;

import de.ks.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.activity.ActivityHint;
import de.ks.standbein.activity.context.ActivityStore;
import de.ks.standbein.application.ApplicationService;
import de.ks.util.FXPlatform;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ActivityDatasourceTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule()).launchServices();

  @Inject
  ActivityController controller;
  @Inject
  ActivityStore store;
  @Inject
  ApplicationService service;

  @After
  public void tearDown() throws Exception {
    controller.stopAll();
  }

  @Test
  public void testDatasourceHint() throws Exception {
    ActivityHint activityHint = new ActivityHint(DatasourceActivity.class);

    controller.startOrResume(activityHint);
    TestDataSource initialDS = (TestDataSource) store.getDatasource();
    assertNull(initialDS.getDataSourceHint());

    ActivityHint nextActivity = new ActivityHint(DatasourceActivity.class);
    nextActivity.setNextActivityId("other");
    nextActivity.setDataSourceHint(() -> "Hallo sauerland!");
    nextActivity.setReturnToActivity(activityHint.getNextActivityId());
    nextActivity.setReturnToDatasourceHint(() -> "back");

    controller.startOrResume(nextActivity);
    TestDataSource datasource = (TestDataSource) store.getDatasource();
    assertEquals("Hallo sauerland!", datasource.getDataSourceHint());

    controller.stopCurrent();

    assertEquals("back", initialDS.getDataSourceHint());
  }

  @Test
  public void testResume() throws Exception {
    ActivityHint activityHint = new ActivityHint(DatasourceActivity.class);
    controller.startOrResume(activityHint);

    ActivityHint nextActivity = new ActivityHint(DatasourceActivity.class);
    nextActivity.setNextActivityId("other");
    controller.startOrResume(nextActivity);

    ActivityHint returnToInitial = new ActivityHint(DatasourceActivity.class);
    returnToInitial.setDataSourceHint(() -> "PCT");
    returnToInitial.setReturnToActivity("other");
    returnToInitial.setReturnToDatasourceHint(() -> "I'll be back");
    controller.startOrResume(returnToInitial);

    TestDataSource datasource = (TestDataSource) store.getDatasource();
    assertEquals("PCT", datasource.getDataSourceHint());

    controller.stopCurrent();

    datasource = (TestDataSource) store.getDatasource();
    assertEquals("I'll be back", datasource.getDataSourceHint());
  }

  @Test
  public void testCustomRunnable() throws Exception {
    ActivityHint activityHint = new ActivityHint(DatasourceActivity.class);
    controller.startOrResume(activityHint);

    CountDownLatch latch = new CountDownLatch(1);

    Runnable runner = () -> {
      try {
        latch.await(60, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        //
      }
    };
    assertFalse(store.loadingProperty().get());

    store.executeCustomRunnable(runner);
    FXPlatform.waitForFX();
    assertTrue(store.loadingProperty().get());

    latch.countDown();
    controller.waitForDataSource();
    FXPlatform.waitForFX();
    assertFalse(store.loadingProperty().get());
  }
}
