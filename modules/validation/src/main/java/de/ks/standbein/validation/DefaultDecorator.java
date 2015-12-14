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
package de.ks.standbein.validation;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.layout.StackPane;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

import java.util.HashMap;
import java.util.Map;

public class DefaultDecorator implements ControlDecorator {
  public static final String CSS_FILE_PATH = "/de/ks/standbein/validation/validationDecorator.css";

  public static final String ERROR_STYLE = "ValidationFailedError";
  public static final String WARNING_STYLE = "ValidationFailedWarning";
  public static final String VALIDATION_DECORATOR_CSS = "validationDecorator.css";
  public static final String VALIDATION_POPUP_STYLE = "ValidationPopup";
  public static final String VALIDATION_POPUP_CONTAINER_STYLE = "ValidationPopupContainer";

  protected final Map<Control, ValidationPopup> toolTips = new HashMap<>();
  protected String styleSheet = VALIDATION_DECORATOR_CSS;

  public void setStyleSheet(String styleSheet) {
    this.styleSheet = styleSheet;
  }

  @Override
  public void decorate(Control c, ValidationResult result) {
    if (c.getScene() == null) {
      ChangeListener<Scene> listener = new ChangeListener<Scene>() {
        @Override
        public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
          if (newValue != null) {
            Platform.runLater(() -> decorate(c, result));
            c.sceneProperty().removeListener(this);
          }
        }
      };
      c.sceneProperty().addListener(listener);
    } else {
      decorateInternal(c, result);
    }
  }

  private void decorateInternal(Control c, ValidationResult result) {
    ValidationMessage highestMessage = result.getHighestMessage();
    ValidationPopup existing = toolTips.get(c);

    if (existing == null) {
      if (highestMessage.getSeverity() == Severity.ERROR) {
        c.getStyleClass().add(ERROR_STYLE);
      } else {
        c.getStyleClass().add(WARNING_STYLE);
      }
      Scene scene = c.getScene();
      Window window = scene.getWindow();
      ValidationPopup tooltip = new ValidationPopup(window);
      tooltip.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_BOTTOM_LEFT);
      existing = tooltip;
      toolTips.put(c, tooltip);
    }
    //FIXME let tooltip stick to position of owner
    existing.setText(highestMessage.getText());
    Bounds bounds = c.localToScreen(c.getLayoutBounds());
    double minY = bounds.getMinY();
    existing.show(c, bounds.getMinX(), minY);
  }

  @Override
  public void removeDecoration(Control c) {
    ValidationPopup existing = toolTips.remove(c);
    if (existing != null) {
      c.getStyleClass().remove(ERROR_STYLE);
      c.getStyleClass().remove(WARNING_STYLE);
      existing.hide();
      existing.window.showingProperty().removeListener(existing.listener);
    }
  }

  class ValidationPopup extends PopupControl {
    private final Window window;
    private Label label = new Label();
    private final ChangeListener<Boolean> listener;

    public ValidationPopup(Window window) {
      this.window = window;
      StackPane pane = new StackPane(label);
      getScene().setRoot(pane);
      getScene().getStylesheets().add(styleSheet);
      label.getStyleClass().add(VALIDATION_POPUP_STYLE);

      listener = (observable, oldValue, newValue) -> {
        if (!newValue) {
          hide();
        }
      };
      window.showingProperty().addListener(listener);

//      DropShadow dropShadow = new DropShadow(10, Color.BLACK);
//      pane.setEffect(dropShadow);
      pane.getStyleClass().add(VALIDATION_POPUP_CONTAINER_STYLE);
    }

    public void setText(String text) {
      label.setText(text);
    }
  }
}
