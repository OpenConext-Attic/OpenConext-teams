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

import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Utility class to generate hash code for the invitations
 */
public final class InvitationHashGenerator {
  private InvitationHashGenerator() {
  }

  /**
   * Generates (unique) hash for invitations
   *
   * @param input String (email address)
   * @return hash code
   */
  public static String generateHash(final String input) {
    return DigestUtils.md5Hex(UUID.nameUUIDFromBytes(input.getBytes()).toString());
  }
}
