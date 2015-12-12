package de.ks;

import de.ks.launch.Launcher;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class LoggingGuiceTestSupportTest {
  @Rule
  public LoggingGuiceTestSupport support = new LoggingGuiceTestSupport(this, new TestModule()).launchServices();

  @Inject
  MyPojo pojo;
  @Inject
  Launcher launcher;
  @Inject
  TestService service;

  @Test
  public void testInjection() throws Exception {
    assertNotNull(pojo);
    assertEquals("Hello Sauerland!", pojo.getText());

  }

  @Test
  public void testLaunching() throws Exception {
    assertTrue(launcher.isStarted());
    assertEquals(1, launcher.getServices().size());
    assertTrue(service.didCallStart);
  }
}