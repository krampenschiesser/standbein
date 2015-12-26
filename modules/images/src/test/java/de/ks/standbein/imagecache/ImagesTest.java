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

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.scene.image.Image;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ForkJoinPool;

import static org.junit.Assert.*;

/**
 *
 */
@SuppressWarnings("SpellCheckingInspection")
public class ImagesTest {
  private static final Logger log = LoggerFactory.getLogger(ImagesTest.class);
  private String packageImage = "packageimage.png";
  private String imageFolderImage = "imageFolderImage.png";
  private String fullyQualified = "/de/ks/standbein/other/otherImage.png";
  private String fileImage;
  private Injector injector;
  private Images images;

  @Before
  public void setUp() throws Exception {
    String workingDirectory = System.getProperty("user.dir");
    log.info("working in {}", workingDirectory);
    if (workingDirectory.endsWith("images")) {
      fileImage = "../../modules/images/fileimage.jpg";
    } else {
      fileImage = "pc/modules/images/fileimage.jpg";
    }

    injector = Guice.createInjector(new ImageModule());
    images = injector.getInstance(Images.class);
  }

  @Test
  public void testSameInstanceInSingleton() throws Exception {
    Images instance1 = injector.getInstance(Images.class);
    Images instance2 = injector.getInstance(Images.class);
    assertSame(instance1, instance2);
  }

  @Test
  public void testFindImages() throws Exception {
    assertNotNull(images.get(fileImage));
    assertNotNull(images.get(packageImage));
    assertNotNull(images.get(imageFolderImage));
    assertNotNull(images.get(fullyQualified));
  }

  @Test
  public void testAsyncImage() throws Exception {
    Image image = images.later(fileImage, ForkJoinPool.commonPool()).get();
    assertNotNull(image);
  }
}
