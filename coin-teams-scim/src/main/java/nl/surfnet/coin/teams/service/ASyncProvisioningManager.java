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

import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.URLEncoder;

import nl.surfnet.coin.teams.model.ScimEvent;
import nl.surfnet.coin.teams.model.ScimMember;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;

/**
 * ASyncProvisioningManager.java
 * 
 */
public class ASyncProvisioningManager implements ProvisioningManager {

  private static final String UTF_8 = "UTF-8";

  private HttpClient client;

  private String baseUri;
  private String username;
  private String password;

  private ObjectMapper mapper = new ObjectMapper().enable(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
      .setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL).setVisibility(JsonMethod.FIELD, Visibility.ANY);

  @Async
  @Override
  public void groupEvent(String teamId, String displayName, Operation operation) {
    try {
      HttpUriRequest request;
      ScimEvent event = new ScimEvent();
      String uriPath = baseUri;
      switch (operation) {
      case CREATE:
        event.setId(teamId);
        event.setDisplayName(displayName);
        request = new HttpPost(uriPath);
        break;
      case DELETE:
        uriPath = uriPath.concat("/").concat(URLEncoder.encode(teamId, UTF_8));
        request = new HttpDelete(uriPath);
        break;
      case UPDATE:
        event.setDisplayName(displayName);
        uriPath = uriPath.concat("/").concat(URLEncoder.encode(teamId, UTF_8));
        request = new HttpPatch(uriPath);
        break;
      default:
        throw new RuntimeException("Unsupported operation for groupEvent(" + operation + ")");
      }
      execute(request, event);
    } catch (Throwable e) {
      // todo - save to database
      throw new RuntimeException(e);
    }
  }

  @Async
  @Override
  public void teamMemberEvent(String teamId, String memberId, String role, Operation operation) {
    try {
      HttpUriRequest request;
      ScimEvent event = new ScimEvent();
      String uriPath = baseUri.concat("/").concat(URLEncoder.encode(teamId, UTF_8));
      request = new HttpPatch(uriPath);
      switch (operation) {
      case CREATE:
        event.setMembers(asList(new ScimMember(memberId, asList(role), null)));
        break;
      case DELETE:
        event.setMembers(asList(new ScimMember(memberId, null, operation.name().toLowerCase())));
        break;
      default:
        throw new RuntimeException("Unsupported operation for teamMemberEvent(" + operation + ")");
      }
      execute(request, event);
    } catch (Throwable e) {
      // todo - save to database
      throw new RuntimeException(e);
    }
  }

  @Async
  @Override
  public void roleEvent(String teamId, String memberId, String role, Operation operation) {
    try {
      HttpUriRequest request;
      ScimEvent event = new ScimEvent();
      String uriPath = baseUri.concat("/").concat(URLEncoder.encode(teamId, UTF_8)).concat("/")
          .concat(URLEncoder.encode(memberId, UTF_8));
      request = new HttpPatch(uriPath);
      switch (operation) {
      case CREATE:
        event.setMembers(asList(new ScimMember(null, asList(role), null)));
        break;
      case DELETE:
        event.setMembers(asList(new ScimMember(null, asList(role), operation.name().toLowerCase())));
        break;
      default:
        throw new RuntimeException("Unsupported operation for roleEvent(" + operation + ")");
      }
      execute(request, event);
    } catch (Throwable e) {
      // todo - save to database
      throw new RuntimeException(e);
    }

  }

  @Override
  public void init(Environment env) {
    this.baseUri = env.getRequiredProperty("provisioner.baseurl").concat("/prov/Groups");
    this.username = env.getRequiredProperty("provisioner.user");
    this.password = env.getRequiredProperty("provisioner.password");
    this.client = new DefaultHttpClient();
  }

  private void execute(HttpUriRequest request, ScimEvent event) throws IOException, JsonGenerationException, JsonMappingException,
      ClientProtocolException {
    request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
    request.addHeader(BasicScheme.authenticate(credentials, UTF_8, false));
    if (request instanceof HttpEntityEnclosingRequest) {
      HttpEntityEnclosingRequest enclosingRequest = (HttpEntityEnclosingRequest) request;
      HttpEntity entity = new StringEntity(mapper.writeValueAsString(event), ContentType.APPLICATION_JSON);
      enclosingRequest.setEntity(entity);
    }
    HttpResponse response = client.execute(request);
    IOUtils.toString(response.getEntity().getContent());
    int status = response.getStatusLine().getStatusCode();
    if (status < 200 || status > 299) {
      throw new RuntimeException("Status = " + status);
    }
  }

}
