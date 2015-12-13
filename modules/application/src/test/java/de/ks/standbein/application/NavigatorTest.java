package de.ks.standbein.application;

import de.ks.standbein.LoggingGuiceTestSupport;
import de.ks.util.FXPlatform;
import javafx.stage.Stage;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class NavigatorTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new ApplicationTestModule()).launchServices();

  @Inject
  ApplicationService service;

  @Test
  public void testSetupStage() throws Exception {
    FXPlatform.waitForFX();

    Stage stage = service.getStage();
    assertNotNull(stage);

    assertEquals(ApplicationTestModule.APP_TITLE, stage.getTitle());
    assertThat(stage.getWidth(), Matchers.greaterThanOrEqualTo((double) ApplicationTestModule.APP_SIZE));
    assertThat(stage.getHeight(), Matchers.greaterThanOrEqualTo((double) ApplicationTestModule.APP_SIZE));
  }
}