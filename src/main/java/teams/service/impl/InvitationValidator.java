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
package teams.service.impl;

import org.hibernate.validator.internal.constraintvalidators.EmailValidator;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import teams.domain.Invitation;

/**
 * Validator for {@link Invitation}
 */
public class InvitationValidator implements Validator {
  private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator();

  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.isAssignableFrom(Invitation.class);
  }

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
