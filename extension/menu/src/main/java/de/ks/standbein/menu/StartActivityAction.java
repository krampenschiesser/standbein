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
package de.ks.standbein.menu;

import com.google.inject.Injector;
import de.ks.standbein.activity.ActivityCfg;
import de.ks.standbein.activity.ActivityController;
import de.ks.standbein.activity.ActivityHint;

import java.util.function.Consumer;

public class StartActivityAction implements Consumer<Injector> {
  protected final Class<? extends ActivityCfg> activity;
  protected boolean refreshOnReturn = true;
  protected String returnToActivity;

  public StartActivityAction(Class<? extends ActivityCfg> activity) {
    this.activity = activity;
  }

  @Override
  public void accept(Injector injector) {
    ActivityController controller = injector.getInstance(ActivityController.class);
    String currentActivity = controller.getCurrentActivityId();
    String returnActivity = currentActivity;
    if (returnToActivity != null) {
      returnActivity = returnToActivity;
    }
    ActivityHint activityHint = new ActivityHint(activity).setReturnToActivity(returnActivity);
    activityHint.setRefreshOnReturn(refreshOnReturn);
    controller.startOrResume(activityHint);
  }

  public StartActivityAction setReturnToActivity(String returnToActivity) {
    this.returnToActivity = returnToActivity;
    return this;
  }

  public StartActivityAction setRefreshOnReturn(boolean refreshOnReturn) {
    this.refreshOnReturn = refreshOnReturn;
    return this;
  }
}
