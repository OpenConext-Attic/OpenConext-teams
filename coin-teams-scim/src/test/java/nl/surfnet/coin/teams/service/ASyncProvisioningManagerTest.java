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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import nl.surfnet.coin.teams.service.ProvisioningManager.Operation;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.env.Environment;

/**
 * RedisSubscriberTestIT.java
 * 
 */
public class ASyncProvisioningManagerTest implements HttpRequestHandler {

  private static LocalTestServerHack localTestServer;

  private static ASyncProvisioningManager provisioningManager;

  private int status;
  private String result;
  private String method;
  private String uri;

  @BeforeClass
  public static void beforeClass() throws Exception {
    localTestServer = new LocalTestServerHack(null, null);
    localTestServer.start();

    /*
     * We don't want async behavior, this is tested somewhere else
     */
    provisioningManager = new ASyncProvisioningManager();
    String baseurl = String.format("http://%s:%d", localTestServer.getServiceAddress().getHostName(), localTestServer.getServiceAddress()
        .getPort());
    Environment env = mock(Environment.class);
    when(env.getRequiredProperty("provisioner.baseurl")).thenReturn(baseurl);
    when(env.getRequiredProperty("provisioner.user")).thenReturn("username");
    when(env.getRequiredProperty("provisioner.password")).thenReturn("password");
    provisioningManager.init(env);
  }

  @Before
  public void before() {
    // we can't do this statically
    localTestServer.register("/prov/*", this);
    status = HttpStatus.SC_OK;
  }

  @Test
  public void testGroupCreate() {
    provisioningManager.groupEvent("teamId", "displayName", Operation.CREATE);
    assertEquals(method, "POST");
    assertEquals(result, "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"id\":\"teamId\",\"displayName\":\"displayName\"}");
  }

  @Test
  public void testGroupUpdate() {
    provisioningManager.groupEvent("teamId", "displayName", Operation.UPDATE);
    assertEquals(method, "PATCH");
    assertEquals(result, "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"displayName\":\"displayName\"}");
    assertEquals("/prov/Groups/teamId", uri);
  }

  @Test
  public void testGroupDelete() {
    provisioningManager.groupEvent("teamId", null, Operation.DELETE);
    assertEquals(method, "DELETE");
    assertEquals(result, null);
    assertEquals("/prov/Groups/teamId", uri);
  }

  @Test
  public void testMemberCreate() {
    provisioningManager.teamMemberEvent("teamId", "memberId", "admin", Operation.CREATE);
    assertEquals(method, "PATCH");
    assertEquals(result, "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"members\":[{\"value\":\"memberId\",\"role\":[\"admin\"]}]}");
    assertEquals("/prov/Groups/teamId", uri);
  }

  @Test
  public void testMemberDelete() {
    provisioningManager.teamMemberEvent("teamId", "memberId", null, Operation.DELETE);
    assertEquals(method, "PATCH");
    assertEquals(result, "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"members\":[{\"value\":\"memberId\",\"operation\":\"delete\"}]}");
    assertEquals("/prov/Groups/teamId", uri);
  }

  @Test
  public void testRoleCreate() {
    provisioningManager.roleEvent("teamId", "memberId", "admin", Operation.CREATE);
    assertEquals(method, "PATCH");
    assertEquals(result, "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"members\":[{\"role\":[\"admin\"]}]}");
    assertEquals("/prov/Groups/teamId/memberId", uri);
  }

  @Test
  public void testRoleDelete() {
    provisioningManager.roleEvent("teamId", "memberId", "manager", Operation.DELETE);
    assertEquals(method, "PATCH");
    assertEquals(result, "{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"members\":[{\"role\":[\"manager\"],\"operation\":\"delete\"}]}");
    assertEquals("/prov/Groups/teamId/memberId", uri);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.http.protocol.HttpRequestHandler#handle(org.apache.http.HttpRequest
   * , org.apache.http.HttpResponse, org.apache.http.protocol.HttpContext)
   */
  @Override
  public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
    assertEquals(BasicScheme.authenticate(new UsernamePasswordCredentials("username", "password"), "UTF-8", false).getValue(), request
        .getFirstHeader("Authorization").getValue());
    assertEquals("application/json", request.getFirstHeader("Content-type").getValue());

    if (request instanceof HttpEntityEnclosingRequest) {
      this.result = IOUtils.toString(((HttpEntityEnclosingRequest) request).getEntity().getContent());
    }
    RequestLine requestLine = request.getRequestLine();
    this.method = requestLine.getMethod();
    this.uri = requestLine.getUri();

    response.setStatusCode(status);

  }

}
