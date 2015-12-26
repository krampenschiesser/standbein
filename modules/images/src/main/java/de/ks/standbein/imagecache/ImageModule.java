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
package de.ks.standbein.imagecache;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.multibindings.OptionalBinder;
import com.google.inject.name.Names;

import javax.inject.Singleton;

public class ImageModule extends AbstractModule {
  public static final String IMG_DEFAULT_PACKAGE = "imagePackage";

  @Override
  protected void configure() {
    OptionalBinder<String> optionalBinder = OptionalBinder.newOptionalBinder(binder(), Key.get(String.class, Names.named(IMG_DEFAULT_PACKAGE)));
    optionalBinder.setDefault().toInstance("/de/ks/standbein/images/");
    bind(Images.class).in(Singleton.class);
  }
}
