/*
 * Copyright 2011 SURFnet bv, The Netherlands
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

package nl.surfnet.coin.teams.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link InvitationHashGenerator}
 */
public class InvitationHashGeneratorTest {
  
  @Test
  public void testGenerateHash() throws Exception {
    String original = "coincalendar@gmail.com";
    String hash = "0b733d119c3705ae4fc284203f1ee8ec";
    assertEquals("Hash code", hash, InvitationHashGenerator.generateHash(original));
  }
}
