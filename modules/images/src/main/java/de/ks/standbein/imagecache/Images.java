/*
 * Copyright [2014] [Christian Loehnert, krampenschiesser@gmail.com]
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ks.standbein.imagecache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;


public class Images {
  private static final Logger log = LoggerFactory.getLogger(Images.class);

  private final LoadingCache<String, Image> cache;
  private final CacheLoader<? super String, Image> loader;

  @Inject
  public Images(@Named(ImageModule.IMG_DEFAULT_PACKAGE) String defaultImagePath) {
    loader = new ImageLoader(defaultImagePath);
    cache = CacheBuilder.newBuilder()//
      .initialCapacity(300)//
      .softValues()//
      .build(loader);
  }

  public Image get(String imagePath) {
    try {
      return cache.get(imagePath);
    } catch (ExecutionException | UncheckedExecutionException e) {
      log.error("Could not load image {}", imagePath, e);
      return null;
    }
  }

  public CompletableFuture<Image> later(String imagePath, ExecutorService executorService) {
    return CompletableFuture.supplyAsync(() -> get(imagePath), executorService);
  }
}
