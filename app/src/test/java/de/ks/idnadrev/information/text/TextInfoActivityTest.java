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

package de.ks.idnadrev.information.text;

import de.ks.LauncherRunner;
import de.ks.activity.ActivityCfg;
import de.ks.idnadrev.ActivityTest;
import de.ks.idnadrev.entity.Tag;
import de.ks.idnadrev.entity.information.TextInfo;
import de.ks.persistence.PersistentWork;
import de.ks.util.FXPlatform;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(LauncherRunner.class)
public class TextInfoActivityTest extends ActivityTest {
  @Override
  protected Class<? extends ActivityCfg> getActivityClass() {
    return TextInfoActivity.class;
  }

  @Test
  public void testCreateNew() throws Exception {
    TextInfoController controller = activityController.getControllerInstance(TextInfoController.class);
    FXPlatform.invokeLater(() -> {
      controller.name.setText("test");
      controller.content.setText("= title\n\nbla");
      controller.tagContainerController.addTag("tag1");
    });
    activityController.save();
    activityController.waitForDataSource();

    PersistentWork.wrap(() -> {
      TextInfo textInfo = PersistentWork.forName(TextInfo.class, "test");
      assertNotNull(textInfo);
      Set<Tag> tags = textInfo.getTags();
      assertEquals(1, tags.size());
      Tag tag = tags.iterator().next();
      assertEquals("tag1", tag.getName());
      assertEquals("= title\n\nbla", textInfo.getContent());
    });
  }

  @Test
  public void testEdit() throws Exception {
    TextInfoDS datasource = (TextInfoDS) store.getDatasource();
    TextInfo textInfo = new TextInfo("test").setDescription("desc");
    PersistentWork.persist(textInfo);
    datasource.setLoadingHint(textInfo);

    activityController.reload();
    activityController.waitForDataSource();

    TextInfoController controller = activityController.getControllerInstance(TextInfoController.class);
    assertEquals("test", controller.name.getText());
    assertEquals("desc", controller.content.getText());

    FXPlatform.invokeLater(() -> controller.content.setText("other"));
    activityController.save();
    activityController.waitForDataSource();

    PersistentWork.wrap(() -> {
      List<TextInfo> from = PersistentWork.from(TextInfo.class);
      assertEquals(1, from.size());
      TextInfo info = from.get(0);
      assertEquals("other", info.getContent());
    });
  }
}