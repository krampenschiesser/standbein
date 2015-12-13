package de.ks.standbein.validation;

import org.junit.Test;

import static org.junit.Assert.*;

public class ValidationResultTest {
  @Test
  public void testCombine() throws Exception {
    ValidationResult result1 = new ValidationResult();
    result1.add(new ValidationMessage("hello"));
    ValidationResult result2 = new ValidationResult();
    result2.add(new ValidationMessage("sauerland"));

    ValidationResult combine = result1.combine(result2);
    assertNotSame(result1, combine);
    assertNotSame(result2, combine);
    assertNotSame(result1.getMessages(), combine.getMessages());

    assertEquals(2, combine.getMessages().size());
    assertEquals("hello", combine.getMessages().get(0).getText());
  }
}