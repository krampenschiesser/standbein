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

import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.stage.PopupWindow;

import java.util.HashMap;
import java.util.Map;

public class DefaultDecorator implements ControlDecorator {
  public static final String ERROR_STYLE = "ValidationFailedError";
  public static final String WARNING_STYLE = "ValidationFailedWarning";

  protected final Map<Control, Tooltip> toolTips = new HashMap<>();

  @Override
  public void decorate(Control c, ValidationResult result) {
    ValidationMessage highestMessage = result.getHighestMessage();
    Tooltip existing = toolTips.get(c);

    if (existing == null) {
      if (highestMessage.getSeverity() == Severity.ERROR) {
        c.getStyleClass().add(ERROR_STYLE);
      } else {
        c.getStyleClass().add(WARNING_STYLE);
      }
      Tooltip tooltip = new Tooltip(highestMessage.getText());
      c.setTooltip(tooltip);
      tooltip.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_BOTTOM_LEFT);
      existing = tooltip;
      toolTips.put(c, tooltip);
    } else {
      existing.setText(highestMessage.getText());
    }
    existing.show(c, 0, c.getHeight());
  }

  @Override
  public void removeDecoration(Control c) {
    Tooltip existing = toolTips.remove(c);
    if (existing != null) {
      c.getStyleClass().remove(ERROR_STYLE);
      c.getStyleClass().remove(WARNING_STYLE);
    }
  }
}
