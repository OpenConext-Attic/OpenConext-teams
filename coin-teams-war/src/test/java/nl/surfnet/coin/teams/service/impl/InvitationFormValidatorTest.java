package nl.surfnet.coin.teams.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import nl.surfnet.coin.teams.domain.InvitationForm;

/**
 * Test for {@link InvitationForm}
 */
public class InvitationFormValidatorTest {

  InvitationFormValidator validator = new InvitationFormValidator();


  @Test
  public void testSupports() throws Exception {
    assertTrue(validator.supports(InvitationForm.class));
  }

  @Test
  public void testValidateForEmailsInput() throws Exception {
    InvitationForm form = new InvitationForm();
    form.setEmails("test@example.com");
    Errors errors = new BeanPropertyBindingResult(form, "invitationForm");
    validator.validate(form, errors);
    assertEquals(0, errors.getErrorCount());
  }

  @Test
  public void testValidateForCSVInput() throws Exception {
    InvitationForm form = new InvitationForm();
    String mails = "test@example.com,test@example.net,john.doe@example.org";
    MultipartFile mockFile = new MockMultipartFile("mockFile", mails.getBytes("utf-8"));
    form.setCsvFile(mockFile);
    Errors errors = new BeanPropertyBindingResult(form, "invitationForm");
    validator.validate(form, errors);
    assertEquals(0, errors.getErrorCount());
  }

  @Test
  public void testValidationFails() throws Exception {
    InvitationForm form = new InvitationForm();
    Errors errors = new BeanPropertyBindingResult(form, "invitationForm");
    validator.validate(form, errors);
    assertEquals(1, errors.getErrorCount());

  }
}
