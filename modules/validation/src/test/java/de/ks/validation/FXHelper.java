/**
 * Copyright [2015] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.validation;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FXHelper {
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  public void startFx() throws InterruptedException {
    Platform.setImplicitExit(true);
    executorService.submit(() -> {
      Application.launch(TestApp.class);
    });
    TestApp.latch.await();
  }

  public void stopFx() {
    Platform.exit();
  }

  public static class TestApp extends Application {
    static CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void start(Stage primaryStage) throws Exception {
      latch.countDown();
    }
  }
}
