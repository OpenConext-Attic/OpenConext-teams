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
    form.setEmails("test@example.com,   test@example.net");
    validator.validate(form, errors);
    assertEquals(0, errors.getErrorCount());
  }

  @Test
  public void testValidateForCSVInput() throws Exception {
    InvitationForm form = new InvitationForm();
    String mails = "test@example.com,test@example.net,john.doe@example.org";
    MultipartFile mockFile = new MockMultipartFile("mockFile", "test.csv",
            "text/csv", mails.getBytes("utf-8"));
    form.setCsvFile(mockFile);
    Errors errors = new BeanPropertyBindingResult(form, "invitationForm");
    validator.validate(form, errors);
    assertEquals(0, errors.getErrorCount());
  }

  @Test
  public void testValidationFails() throws Exception {
    InvitationForm form = new InvitationForm();
    form.setEmails("must.be,valid@example.com");
    Errors errors = new BeanPropertyBindingResult(form, "invitationForm");
    validator.validate(form, errors);
    assertEquals(1, errors.getErrorCount());
  }

}
