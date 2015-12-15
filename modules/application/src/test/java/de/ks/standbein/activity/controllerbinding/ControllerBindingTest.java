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
package de.ks.standbein.activity.controllerbinding;

import de.ks.IntegrationTestModule;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.activity.ActivityHint;
import de.ks.standbein.activity.context.ActivityStore;
import de.ks.standbein.application.ApplicationService;
import de.ks.util.FXPlatform;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class ControllerBindingTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new IntegrationTestModule()).launchServices();

  @Inject
  ActivityController controller;
  @Inject
  ActivityStore store;
  @Inject
  ApplicationService service;

  private TestBindingDS datasource;

  @Before
  public void setUp() throws Exception {
    controller.startOrResume(new ActivityHint(BindingActivity.class));
    controller.waitForTasks();
    datasource = (TestBindingDS) store.getDatasource();
  }

  @After
  public void tearDown() throws Exception {
    controller.stopAll();
  }

  @Test
  public void testNameBinding() throws Exception {
    GridPane gridPane = controller.getCurrentNode();
    TextField name = (TextField) gridPane.lookup("#name");
    assertEquals("test", name.getText());

    FXPlatform.invokeLater(() -> name.setText("Hello"));
    TestBindingController ctrl = controller.getCurrentController();
    ctrl.save();
    controller.waitForTasks();


    Option hello = datasource.getSaved();
    assertNotNull(hello);
    assertEquals("Hello", hello.getName());

    datasource.getSaved().setName("Test");
    controller.reload();
    controller.waitForTasks();
    assertEquals("Test", name.getText());
  }

  @Test
  public void testClearOnRefresh() throws Exception {
    GridPane gridPane = controller.getCurrentNode();
    TextField name = (TextField) gridPane.lookup("#name");
    assertEquals("test", name.getText());

    store.getBinding().registerClearOnRefresh(name);
    FXPlatform.invokeLater(() -> store.getBinding().getStringProperty(Option.class, o -> o.getName()).unbindBidirectional(name.textProperty()));

    controller.reload();
    controller.waitForDataSource();

    assertEquals("", name.getText());
  }
}
