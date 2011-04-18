package nl.surfnet.coin.teams.service.impl;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import nl.surfnet.coin.teams.domain.InvitationForm;

/**
 * Validates {@link InvitationForm}
 */
public class InvitationFormValidator implements Validator {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(InvitationForm.class);
  }

  /**
   * {@inheritDoc}
   * <p/>
   * Fails if both the input field for manual email address input is empty and there is no
   * csv file with content
   */
  @Override
  public void validate(Object target, Errors errors) {
    InvitationForm form = (InvitationForm) target;

    MultipartFile csvFile = form.getCsvFile();
    if (!(StringUtils.hasText(form.getEmails())) && (!form.hasCsvFile() || csvFile.getSize() == 0)) {
      errors.rejectValue("emails", "invite.errors.NoEmailAddresses");
    }
  }
}
