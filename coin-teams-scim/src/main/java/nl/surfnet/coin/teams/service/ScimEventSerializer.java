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

import nl.surfnet.coin.teams.model.ScimEvent;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;

/**
 * ScimEventSerializer.java
 * 
 */
public class ScimEventSerializer {

  public String serialize(ScimEvent t) {
    byte[] serialize = SerializationUtils.serialize(t);
    return serialize == null ? null : Base64.encodeBase64String(serialize);
  }

  public ScimEvent deserialize(String s) {
    return StringUtils.hasText(s) ? (ScimEvent) SerializationUtils.deserialize(Base64.decodeBase64(s)) : null;
  }

}
