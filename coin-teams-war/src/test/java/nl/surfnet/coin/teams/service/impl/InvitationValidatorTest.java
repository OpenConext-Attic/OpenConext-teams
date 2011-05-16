package nl.surfnet.coin.teams.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import nl.surfnet.coin.teams.domain.Invitation;

/**
 * Test for {@link InvitationValidator}
 */
public class InvitationValidatorTest {
  private Validator validator = new InvitationValidator();

  @Test
  public void testSupports() throws Exception {
    assertTrue(validator.supports(Invitation.class));
  }

  @Test
  public void testValidate() throws Exception {
    Invitation invitation = new Invitation("valid@example.com", null);
    Errors errors = new BeanPropertyBindingResult(invitation, "invitation");
    validator.validate(invitation, errors);
    assertEquals(0, errors.getErrorCount());
  }

  @Test
  public void testFailOnNoEmail() throws Exception {
    Invitation invitation = new Invitation("", null);
    Errors errors = new BeanPropertyBindingResult(invitation, "invitation");
    validator.validate(invitation, errors);
    assertEquals(1, errors.getErrorCount());
  }

  @Test
  public void testFailOnInvalidEmail() throws Exception {
    Invitation invitation = new Invitation("invalid.email.example.com", null);
    Errors errors = new BeanPropertyBindingResult(invitation, "invitation");
    validator.validate(invitation, errors);
    assertEquals(1, errors.getErrorCount());
  }

}
