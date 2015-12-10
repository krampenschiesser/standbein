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

package de.ks.eventsystem;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import de.ks.eventsystem.bus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

public class EventBusModule extends AbstractModule {

  @Override
  protected void configure() {
    //nope just provider
  }

  @Provides
  @Singleton
  public EventBus getEventBus() {
    return new EventBus().register(new LoggingDeadEventHandler());
  }

  static class LoggingDeadEventHandler {
    private static final Logger log = LoggerFactory.getLogger(LoggingDeadEventHandler.class);

    @Subscribe
    public void onDeadEvent(DeadEvent dead) {
      log.warn("No handler for event \"{}\" found. Contents: {}", dead.getEvent().getClass().getSimpleName(), dead.getEvent());
    }
  }
}
