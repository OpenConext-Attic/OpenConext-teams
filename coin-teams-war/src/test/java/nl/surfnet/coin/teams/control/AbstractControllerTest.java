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

package nl.surfnet.coin.teams.control;

import nl.surfnet.coin.teams.domain.Member;
import nl.surfnet.coin.teams.domain.Role;
import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.domain.TeamResultWrapper;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;
import org.junit.Before;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.stubbing.Answer;
import org.opensocial.models.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.mock;

/**
 * Base class for testing {@link Controller} instances
 */
public abstract class AbstractControllerTest {

  private MockHttpServletRequest request;
  private ModelMap modelMap;

  /**
   * Autowire all dependencies with a annotation autowired with a mock that does
   * nothing
   *
   * @param target the controller
   */
  protected void autoWireRemainingResources(Object target) throws Exception {
    Class<? extends Object> clazz = target.getClass();
    while (!clazz.equals(Object.class)) {
      doAutoWireRemainingResources(target, clazz.getDeclaredFields());
      clazz = clazz.getSuperclass();
    }
  }

  private void doAutoWireRemainingResources(Object target, Field[] fields)
          throws IllegalAccessException {
    for (Field field : fields) {
      ReflectionUtils.makeAccessible(field);
      if (field.getAnnotation(Autowired.class) != null
              && field.get(target) == null) {
        field.set(target, mock(field.getType(), new DoesNothing()));
      }
    }
  }

  protected Returns getMyTeamReturn() {
    List<Team> teams = new ArrayList<Team>();
    Team team1 = new Team("team-1", "Team 1", "Description team 1");
    Team team2 = new Team("team-2", "Team 2", "Description team 2");
    Team team3 = new Team("team-3", "Team 3", "Description team 3");
    teams.add(team1);
    teams.add(team2);
    teams.add(team3);
    TeamResultWrapper resultWrapper = new TeamResultWrapper(teams, teams.size(), 0, 10);
    return new Returns(resultWrapper);
  }

  protected Returns getAllTeamReturn() {
    List<Team> teams = new ArrayList<Team>();
    Team team1 = new Team("team-1", "Team 1", "Description team 1");
    Team team2 = new Team("team-2", "Team 2", "Description team 2");
    Team team3 = new Team("team-3", "Team 3", "Description team 3");
    Team team4 = new Team("team-4", "Team 4", "Description team 4");
    Team team5 = new Team("team-5", "Team 5", "Description team 5");
    Team team6 = new Team("team-6", "Team 6", "Description team 6");
    teams.add(team1);
    teams.add(team2);
    teams.add(team3);
    teams.add(team4);
    teams.add(team5);
    teams.add(team6);
    TeamResultWrapper resultWrapper = new TeamResultWrapper(teams, teams.size(), 0, 10);
    return new Returns(resultWrapper);
  }

  protected Team getTeam1() {
    return new Team("team-1", "Team 1", "Nice description", true);
  }

  protected Person getPerson1() {
    Person person = new Person();
    person.setField("id", "member-1");
    return person;
  }

  protected Member getAdministrativeMember() {
    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Manager);
    roles.add(Role.Member);
    roles.add(Role.Admin);
    return new Member(roles, "Member 1", "member-1", "member@example.com");
  }

  protected Member getMember() {
    HashSet<Role> roles = new HashSet<Role>();
    roles.add(Role.Member);
    return new Member(roles, "Member 1", "member-1", "member@example.com");
  }

  protected Returns getSearchTeamReturn() {
    ArrayList<Team> teams = new ArrayList<Team>();
    Team team1 = new Team("team-1", "Team 1", "Description team 1");
    teams.add(team1);
    TeamResultWrapper resultWrapper = new TeamResultWrapper(teams, teams.size(), 0, 10);
    return new Returns(resultWrapper);
  }

  /**
   * @param target         the controller
   * @param answer         the answer to return on method invocations
   * @param interfaceClass the class to mock
   */
  @SuppressWarnings("unchecked")
  protected void autoWireMock(Object target, Answer answer, Class interfaceClass)
          throws Exception {
    Object mock = mock(interfaceClass, answer);
    autoWireMock(target, mock, interfaceClass);
  }

  /**
   * @param target         the controller
   * @param mock           the mock Object to return on method invocations
   * @param interfaceClass the class to mock
   */
  @SuppressWarnings("unchecked")
  protected void autoWireMock(Object target, Object mock, Class interfaceClass)
          throws Exception {
    boolean found = doAutoWireMock(target, mock, interfaceClass, target
            .getClass().getDeclaredFields());
    if (!found) {
      doAutoWireMock(target, mock, interfaceClass, target.getClass()
              .getSuperclass().getDeclaredFields());
    }
  }

  private boolean doAutoWireMock(Object target, Object mock,
                                 Class interfaceClass, Field[] fields) throws IllegalAccessException {
    boolean found = false;
    for (Field field : fields) {
      if (field.getType().equals(interfaceClass)) {
        ReflectionUtils.makeAccessible(field);
        field.set(target, mock);
        found = true;
        break;
      }
    }
    return found;
  }

  /**
   * Put the Groups and Person in the session
   *
   * @param request the HttpServletRequest
   */
  private void setUpSession(HttpServletRequest request) {
    HttpSession session = request.getSession(true);

    Person person = new Person();
    person.setField("id", "member-1");
    session.setAttribute(LoginInterceptor.PERSON_SESSION_KEY, person);
    session.setAttribute(LoginInterceptor.USER_STATUS_SESSION_KEY, "member");

  }

  @Before
  public void setup() throws Exception {
    this.request = new MockHttpServletRequest();
    this.modelMap = new ModelMap();
    setUpSession(request);
  }

  /**
   * @return the request
   */
  protected MockHttpServletRequest getRequest() {
    return request;
  }

  /**
   * @return the modelMap
   */
  protected ModelMap getModelMap() {
    return modelMap;
  }
}
