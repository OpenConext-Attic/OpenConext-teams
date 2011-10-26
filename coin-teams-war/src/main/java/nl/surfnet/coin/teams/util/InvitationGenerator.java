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

import org.apache.commons.lang.RandomStringUtils;

/**
 * Utility class to generate hash code for the invitations
 */
public final class InvitationGenerator {

  private static final int INVITATION_HASH_LENGTH = 255;
  private InvitationGenerator() {
  }

  /**
   * Generates (unique) ramdom string for invitations
   *
   * @return random {@link String}
   */
  public static String generateHash() {
    return RandomStringUtils.randomAlphanumeric(INVITATION_HASH_LENGTH);
  }
}
