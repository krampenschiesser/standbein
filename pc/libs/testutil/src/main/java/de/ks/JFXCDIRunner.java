package de.ks;
/*
 * Created by Christian Loehnert
 * Krampenschiesser@freenet.de
 * All rights reserved by now, license may come later.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class JFXCDIRunner extends BlockJUnit4ClassRunner {
  private static final Logger log = LoggerFactory.getLogger(JFXCDIRunner.class);

  public static Stage getStage() {
    return JFXTestApp.stage;
  }

  public static class JFXTestApp extends Application {
    public static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
      barrier.countDown();
      JFXTestApp.stage = stage;
    }
  }

  private static final ExecutorService executor = Executors.newCachedThreadPool();
  private static final CountDownLatch barrier = new CountDownLatch(2);
  private static final CdiContainer cdiContainer = CdiContainerLoader.getCdiContainer();

  public JFXCDIRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  public void run(RunNotifier notifier) {
    if (barrier.getCount() > 0) {
      startApp();
    }
    notifier.addListener(new RunListener() {
      @Override
      public void testStarted(Description description) throws Exception {
        super.testStarted(description);
        log.info("@Test:{}.{}", description.getClassName(), description.getMethodName());
      }
    });
    super.run(notifier);
//    stopApp();
  }

  @Override
  protected Object createTest() throws Exception {
    return CDI.current().select(getTestClass().getJavaClass()).get();
  }

  public static void startApp() {
    if (barrier.getCount() == 2) {
      executor.execute(() -> {
        Application.launch(JFXTestApp.class);
      });
    }
    executor.execute(() -> {
      try {
        cdiContainer.boot();
      } finally {
        barrier.countDown();
      }
    });
    try {
      if (!barrier.await(5, TimeUnit.MINUTES)) {
        stopApp();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void stopApp() {
    Platform.exit();
    cdiContainer.shutdown();
  }
}
