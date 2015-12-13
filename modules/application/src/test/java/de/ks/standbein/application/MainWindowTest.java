package de.ks.standbein.application;

import com.google.inject.Injector;
import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.util.FXPlatform;
import javafx.scene.Node;
import javafx.scene.Scene;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class MainWindowTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new ApplicationTestModule().useMainWindow()).launchServices();

  @Inject
  Navigator navigator;
  @Inject
  MainTestWindow mainWindow;

  @Inject
  Injector injector;

  @Test
  public void testMainWindowAssignment() throws Exception {
    FXPlatform.waitForFX();
    Node currentNode = navigator.getCurrentNode();
    assertNotNull(currentNode);

    assertNotNull(mainWindow.stackPane);
    Scene scene = mainWindow.stackPane.getScene();
    assertNotNull(scene);
    assertEquals(currentNode, mainWindow.stackPane);
  }
}