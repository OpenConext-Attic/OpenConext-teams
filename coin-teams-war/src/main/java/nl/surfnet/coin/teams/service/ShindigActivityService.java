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

/**
 * 
 */
package nl.surfnet.coin.teams.service;

/**
 * @author steinwelberg
 * 
 */
public interface ShindigActivityService {

  /**
   * Add the activity to Shindig
   * 
   * @param personId
   *          The id of the {@link org.opensocial.models.Person}
   * @param teamId
   *          the id of the {@link nl.surfnet.coin.teams.domain.Team}
   * @param title
   *          the title of the activity
   * @param body
   *          the body of the activity
   * 
   */
  void addActivity(String personId, String teamId, String title, String body);


}
