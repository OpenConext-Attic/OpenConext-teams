/*
 * Copyright 2012 SURFnet bv, The Netherlands
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
package teams.control;

import com.google.common.base.MoreObjects;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import teams.domain.Language;
import teams.domain.Role;

import java.io.IOException;
import java.io.InputStream;

public class InvitationForm {

  private String emails;
  private String message;
  private String teamId;
  private MultipartFile csvFile;
  private Role intendedRole = Role.Member;
  private Language language;

  public String getEmails() {
    return emails;
  }

  public char[] getCsvFileEmails() throws IOException {
    try (InputStream inputStream = csvFile.getInputStream()) {
      return IOUtils.toCharArray(inputStream);
    }
  }

  public void setEmails(String emails) {
    this.emails = emails;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getTeamId() {
    return teamId;
  }

  public void setTeamId(String teamId) {
    this.teamId = teamId;
  }

  public MultipartFile getCsvFile() {
    return csvFile;
  }

  public void setCsvFile(MultipartFile csvFile) {
    this.csvFile = csvFile;
  }

  public boolean hasCsvFile() {
    return csvFile!=null && StringUtils.hasText(csvFile.getOriginalFilename());
  }

  public Role getIntendedRole() {
    return intendedRole;
  }

  public void setIntendedRole(Role intendedRole) {
    this.intendedRole = intendedRole;
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("teamId", teamId).toString();
  }

}
