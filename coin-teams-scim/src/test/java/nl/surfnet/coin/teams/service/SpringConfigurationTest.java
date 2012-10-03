/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.surfnet.coin.teams.service;

import static org.junit.Assert.assertNotNull;

import javax.annotation.Resource;

import nl.surfnet.coin.teams.config.SpringConfiguration;
import nl.surfnet.coin.teams.service.ProvisioningManager.Operation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * SpringConfigurationTest.java
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfiguration.class)
public class SpringConfigurationTest {

  @Resource(name = "provisioningManager")
  private ProvisioningManager provisioningManager;

  @Test
  public void testConfiguration() {
    assertNotNull(provisioningManager);
    /*
     * Normally this would end in an Exception, but as we are async this
     * immediately returns
     */
    provisioningManager.groupEvent("teamId", "displayName", Operation.CREATE);
  }

}
