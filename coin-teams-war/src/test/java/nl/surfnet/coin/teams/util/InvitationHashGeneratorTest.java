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
