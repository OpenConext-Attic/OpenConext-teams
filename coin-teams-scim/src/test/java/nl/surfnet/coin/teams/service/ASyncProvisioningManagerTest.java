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
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.env.Environment;

/**
 * {@link Test} for {@link ASyncProvisioningManager}
 * 
 */
public class ASyncProvisioningManagerTest implements HttpRequestHandler {

  private static final String PASSWORD = "password";
  private static final String USERNAME = "username";

  private static LocalTestServer localTestServer;

  private static ASyncProvisioningManager provisioningManager;

  private int status;
  private String result;
  private String method;
  private String uri;

  @BeforeClass
  public static void beforeClass() throws Exception {
    localTestServer = new LocalTestServerPatch(  null, null);
    localTestServer.start();

    /*
     * We don't want async behavior, this is tested somewhere else
     */
    provisioningManager = new ASyncProvisioningManager();
    String baseurl = String.format("http://%s:%d", localTestServer.getServiceAddress().getHostName(), localTestServer.getServiceAddress()
        .getPort());
    initEnv(provisioningManager, baseurl);
  }

  private static void initEnv(ProvisioningManager manager, String baseurl) {
    Environment env = mock(Environment.class);
    when(env.getRequiredProperty("provisioner.baseurl")).thenReturn(baseurl);
    when(env.getRequiredProperty("provisioner.user")).thenReturn(USERNAME);
    when(env.getRequiredProperty("provisioner.password")).thenReturn(PASSWORD);
    manager.init(env);
  }

  @Before
  public void before() {
    // we can't do this statically
    localTestServer.register("/Groups/*", this);
    localTestServer.register("/extra/*", this);
    status = HttpStatus.SC_OK;
  }

  @Test
  public void testGroupCreate() {
    provisioningManager.groupEvent("teamId", "displayName", Operation.CREATE);
    assertEquals(method, "POST");
    assertEquals("{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"id\":\"teamId\",\"displayName\":\"displayName\"}", result);
    assertEquals("/Groups/v1.1", uri);
  }

  @Test
  public void testGroupUpdate() {
    provisioningManager.groupEvent("teamId", "displayName", Operation.UPDATE);
    assertEquals("PATCH", method);
    assertEquals("{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"displayName\":\"displayName\"}", result);
    assertEquals("/Groups/v1.1/teamId", uri);
  }

  @Test
  public void testGroupDelete() {
    provisioningManager.groupEvent("teamId", null, Operation.DELETE);
    assertEquals("DELETE", method);
    assertEquals(null, result);
    assertEquals("/Groups/v1.1/teamId", uri);
  }

  @Test
  public void testMemberCreate() {
    provisioningManager.teamMemberEvent("teamId", "memberId", "admin", Operation.CREATE);
    assertEquals("PATCH", method);
    assertEquals("{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"members\":[{\"value\":\"memberId\",\"role\":[\"admin\"]}]}", result);
    assertEquals("/Groups/v1.1/teamId", uri);
  }

  @Test
  public void testMemberDelete() {
    provisioningManager.teamMemberEvent("teamId", "memberId", null, Operation.DELETE);
    assertEquals("PATCH", method);
    assertEquals("{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"members\":[{\"value\":\"memberId\",\"operation\":\"delete\"}]}", result);
    assertEquals("/Groups/v1.1/teamId", uri);
  }

  @Test
  public void testRoleCreate() {
    provisioningManager.roleEvent("teamId", "memberId", "admin", Operation.CREATE);
    assertEquals("PATCH", method);
    assertEquals("{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"members\":[{\"role\":[\"admin\"]}]}", result);
    assertEquals("/extra/Groups/v1.1/teamId/memberId", uri);
  }

  @Test
  public void testRoleDelete() {
    provisioningManager.roleEvent("teamId", "memberId", "manager", Operation.DELETE);
    assertEquals("PATCH", method);
    assertEquals("{\"schemas\":[\"urn:scim:schemas:core:1.0\"],\"members\":[{\"role\":[\"manager\"],\"operation\":\"delete\"}]}", result);
    assertEquals("/extra/Groups/v1.1/teamId/memberId", uri);
  }

  @Test
  public void testNoopManager() {
    NoOpProvisioningManager manager = new NoOpProvisioningManager();
    String baseurl = String.format("http://%s:%d", localTestServer.getServiceAddress().getHostName(), localTestServer.getServiceAddress()
        .getPort());
    initEnv(manager, baseurl);
    manager.roleEvent("teamId", "memberId", "admin", Operation.CREATE);
  }

  /*
   * Here we respond to the actual HTTP call and save the body content, method
   * and url in class variables to make assertions against.
   */
  @Override
  public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
    assertEquals(BasicScheme.authenticate(new UsernamePasswordCredentials(USERNAME, PASSWORD), "UTF-8", false).getValue(), request
        .getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
    assertEquals(ContentType.APPLICATION_JSON.getMimeType(), request.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue());

    if (request instanceof HttpEntityEnclosingRequest) {
      this.result = IOUtils.toString(((HttpEntityEnclosingRequest) request).getEntity().getContent());
    }
    RequestLine requestLine = request.getRequestLine();
    this.method = requestLine.getMethod();
    this.uri = requestLine.getUri();

    response.setStatusCode(status);

  }

}
