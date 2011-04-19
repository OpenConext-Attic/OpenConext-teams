package nl.surfnet.coin.teams.service.impl;

import org.hibernate.validator.constraints.impl.EmailValidator;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import nl.surfnet.coin.teams.domain.Invitation;

/**
 * Validator for {@link Invitation}
 */
public class InvitationValidator implements Validator {
  private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator();

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(Invitation.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(Object target, Errors errors) {
    Invitation invitation = (Invitation) target;

    if (!StringUtils.hasText(invitation.getEmail())) {
      errors.rejectValue("email", "error.RequiredField");
    }

    if (!EMAIL_VALIDATOR.isValid(invitation.getEmail(), null)) {
      errors.rejectValue("email", "error.IncorrectEmailFormat");
    }
  }
}
