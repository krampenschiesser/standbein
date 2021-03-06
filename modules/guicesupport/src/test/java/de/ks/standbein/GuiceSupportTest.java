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
package de.ks.standbein;

import com.google.inject.Guice;
import com.google.inject.Module;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class GuiceSupportTest {
  @Test
  public void testModuleDiscovery() throws Exception {
    GuiceSupport instance = GuiceSupport.instance;
    Collection<Module> modules = instance.discoverModules();
    assertEquals(2, modules.size());
  }

  @Test
  public void testInstanceCreation() throws Exception {
    TestPojo select = GuiceSupport.get(TestPojo.class);
    assertNotNull(select);
    assertEquals("Sauerland", select.getName());
  }

  @Test
  public void testGenericInjection() throws Exception {
    Injectioned injectioned = GuiceSupport.get(Injectioned.class);
    TestPojo instance = injectioned.injector.getInstance(TestPojo.class);
    assertNotNull(instance);
  }

  @Test
  public void testPrivateSetterInjection() throws Exception {
    Injectioned injectioned = GuiceSupport.get(Injectioned.class);
    TestPojo instance = injectioned.pojo;
    assertNotNull(instance);
  }

  @Test
  public void testMultiBindPojo() throws Exception {
    MultiBindPojo pojo = GuiceSupport.get(MultiBindPojo.class);
    assertEquals(5, pojo.getStyles().size());
  }

  @Test
  public void testMultiBindPojoUnBound() throws Exception {
    MultiBindPojo pojo = Guice.createInjector(new Module1()).getInstance(MultiBindPojo.class);
    assertNull(pojo.getStyles());
  }
}
