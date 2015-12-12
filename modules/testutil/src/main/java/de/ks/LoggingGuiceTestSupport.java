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
package de.ks;

import com.google.inject.Module;
import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingGuiceTestSupport extends GuiceTestSupport {
  private final Logger log;

  public LoggingGuiceTestSupport(Object test, Module... modules) {
    super(test, modules);
    log = LoggerFactory.getLogger(test.getClass());
  }

  @Override
  public LoggingGuiceTestSupport launchServices() {
    super.launchServices();
    return this;
  }

  @Override
  public LoggingGuiceTestSupport preventServiceStop() {
    super.preventServiceStop();
    return this;
  }

  @Override
  protected void starting(Description description) {
    super.starting(description);
    log.info("###Starting {}.{}", description.getTestClass().getSimpleName(), description.getMethodName());
  }

  @Override
  protected void succeeded(Description description) {
    super.succeeded(description);
    log.info("###Successfully finished {}.{}", description.getTestClass().getSimpleName(), description.getMethodName());
  }

  @Override
  protected void failed(Throwable e, Description description) {
    super.failed(e, description);
    log.error("###Failed {}.{}: {}", description.getTestClass().getSimpleName(), description.getMethodName(), e, e);
  }

  @Override
  protected void skipped(AssumptionViolatedException e, Description description) {
    super.skipped(e, description);
    log.info("###Skipped {}.{}: {}", description.getTestClass().getSimpleName(), description.getMethodName());
  }
}
