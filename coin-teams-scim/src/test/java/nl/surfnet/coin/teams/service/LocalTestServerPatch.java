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

import org.apache.http.HttpRequestFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;

/**
 * We need support for the 'PATCH' method.
 * 
 * See https://issues.apache.org/jira/browse/HTTPCLIENT-1239
 * 
 */
public class LocalTestServerPatch extends LocalTestServer {

  /**
   * @param proc
   * @param params
   */
  public LocalTestServerPatch(BasicHttpProcessor proc, HttpParams params) {
    super(proc, params);
  }

  @Override
  protected DefaultHttpServerConnection createHttpServerConnection() {
    return new DefaultHttpServerConnection() {
      @Override
      protected HttpRequestFactory createHttpRequestFactory() {
        return new LenientHttpRequestFactory();
      }
    };
  }

}
