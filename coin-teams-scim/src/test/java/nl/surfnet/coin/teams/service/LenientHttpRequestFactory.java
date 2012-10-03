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

import org.apache.http.HttpRequest;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.RequestLine;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;

/**
 * See https://issues.apache.org/jira/browse/HTTPCLIENT-1239
 * 
 */
public class LenientHttpRequestFactory extends DefaultHttpRequestFactory {

  @Override
  public HttpRequest newHttpRequest(RequestLine requestline) throws MethodNotSupportedException {
    try {
      return super.newHttpRequest(requestline);
    } catch (MethodNotSupportedException e) {
      return new BasicHttpEntityEnclosingRequest(requestline);
    }
  }

  @Override
  public HttpRequest newHttpRequest(String method, String uri) throws MethodNotSupportedException {
    try {
      return super.newHttpRequest(method, uri);
    } catch (MethodNotSupportedException e) {
      return new BasicHttpEntityEnclosingRequest(method, uri);
    }
  }

}
