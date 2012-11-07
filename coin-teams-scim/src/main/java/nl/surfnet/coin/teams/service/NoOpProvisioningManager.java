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
package nl.surfnet.coin.teams.service;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;

/**
 * NoOpProvisioningManager.java
 * 
 */
public class NoOpProvisioningManager extends ASyncProvisioningManager {

  @Override
  protected void doExecute(HttpUriRequest request) throws IOException, ClientProtocolException {
    String json = "";
    if (request instanceof HttpEntityEnclosingRequest) {
      json = IOUtils.toString(((HttpEntityEnclosingRequest) request).getEntity().getContent());
    }
    log.info("Broadcasting team change (" + request.getMethod() + " : " + json + ")");
  }

  @Override
  public void groupEvent(String teamId, String displayName, Operation operation) {
    super.groupEvent(teamId, displayName, operation);
  }

  @Override
  public void teamMemberEvent(String teamId, String memberId, String role, Operation operation) {
    super.teamMemberEvent(teamId, memberId, role, operation);
  }

  @Override
  public void roleEvent(String teamId, String memberId, String role, Operation operation) {
    super.roleEvent(teamId, memberId, role, operation);
  }


}
