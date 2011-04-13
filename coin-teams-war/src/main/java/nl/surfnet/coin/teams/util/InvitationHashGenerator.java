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
