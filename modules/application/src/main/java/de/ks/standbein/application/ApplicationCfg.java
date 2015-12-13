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
package de.ks.standbein.application;

public class ApplicationCfg {
  private String title;
  private String icon;
  private double width;
  private double height;
  private boolean localized;

  public ApplicationCfg(String title, double width, double height) {
    this.title = title;
    this.width = width;
    this.height = height;
  }

  public ApplicationCfg setIcon(String icon) {
    this.icon = icon;
    return this;
  }

  public ApplicationCfg setLocalized(boolean localized) {
    this.localized = localized;
    return this;
  }

  public boolean isLocalized() {
    return localized;
  }

  public String getTitle() {
    return title;
  }

  public String getIcon() {
    return icon;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }
}
