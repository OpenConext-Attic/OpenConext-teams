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

import nl.surfnet.coin.teams.domain.Invitation;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
