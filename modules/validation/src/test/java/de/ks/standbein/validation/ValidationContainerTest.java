package de.ks.standbein.validation;

import de.ks.util.FXPlatform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class ValidationContainerTest {
  static final FXHelper helper = new FXHelper();

  @BeforeClass
  public static void initFX() throws InterruptedException {
    helper.startFx();
  }

  @AfterClass
  public static void stopFx() throws InterruptedException {
    helper.stopFx();
  }

  @Test
  public void testCustomDecorator() throws Exception {
    ValidationContainer container = new ValidationContainer();

    ValidationResult result = new ValidationResult().add(new ValidationMessage("test"));

    Control control = Mockito.mock(Control.class);
    ObservableValue observable = Mockito.mock(ObservableValue.class);
    ControlDecorator decorator = Mockito.mock(ControlDecorator.class);
    Mockito.atLeastOnce();

    container.observableValues.put(control, observable);
    container.registerDecoratorForControl(control, decorator);

    container.handleChange((o, o2) -> result, control, null);
    FXPlatform.invokeLater(this::noop);
    Mockito.verify(decorator, Mockito.times(1)).decorate(control, result);

    Mockito.reset(decorator);
    container.handleChange((o, o2) -> null, control, null);
    FXPlatform.invokeLater(this::noop);
    Mockito.verify(decorator, Mockito.times(1)).removeDecoration(control);
  }

  @Test
  public void testValidationCycle() throws Exception {
    TextField textField = new TextField();

    ValidationContainer container = new ValidationContainer();
    container.registerValidator(textField, (t, s) -> {
      if (s != null && s.equals("Sauerland!")) {
        return null;
      } else {
        return ValidationResult.createError("NO");
      }
    });
    FXPlatform.invokeLater(this::noop);
    assertTrue(container.isInvalid());
    assertEquals(1, container.results.size());
    assertEquals(1, container.getValidationResult().getMessages().size());
    assertEquals("NO", container.getValidationResult().getMessages().get(0).getText());

    FXPlatform.invokeLater(() -> textField.setText("Sauerland!"));
    FXPlatform.invokeLater(this::noop);
    assertFalse(container.isInvalid());
    assertEquals(0, container.results.size());
    assertNull(container.getValidationResult());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDuplicateRegistration() throws Exception {
    ValidationContainer container = new ValidationContainer();
    Control control = Mockito.mock(Control.class);
    ObservableValue<String> observable = Mockito.mock(ObservableValue.class);

    Validator<Control, String> validator1 = (t, s) -> null;
    Validator<Control, String> validator2 = (t, s) -> null;
    container.registerValidator(control, validator1, observable);
    container.registerValidator(control, validator2, observable);

    FXPlatform.invokeLater(this::noop);

    Mockito.verify(observable, Mockito.times(2)).addListener((ChangeListener<? super String>) Mockito.any());
    Mockito.verify(observable, Mockito.times(1)).removeListener((ChangeListener<? super String>) Mockito.any());


    //check that we created a combined validator that keeps the order of validators
    assertEquals(1, container.validators.size());
    Validator validator = container.validators.values().iterator().next();
    assertThat(validator, Matchers.instanceOf(Validator.CombinedValidator.class));
    Validator.CombinedValidator cast = (Validator.CombinedValidator) validator;
    assertEquals(2, cast.validators.size());

    container.registerValidator(control, validator1, observable);
    assertEquals(3, cast.validators.size());
  }

  private void noop() {
  }

}