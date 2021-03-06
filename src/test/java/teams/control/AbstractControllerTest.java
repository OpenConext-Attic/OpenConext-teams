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

import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.Controller;

import teams.domain.Member;
import teams.domain.Person;
import teams.domain.Role;
import teams.domain.Stem;
import teams.domain.Team;
import teams.domain.TeamResultWrapper;
import teams.interceptor.LoginInterceptor;

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

  protected TeamResultWrapper getMyTeams() {
    List<Team> teams = new ArrayList<>();
    Team team1 = new Team("team-1", "Team 1", "Description team 1");
    Team team2 = new Team("team-2", "Team 2", "Description team 2");
    Team team3 = new Team("team-3", "Team 3", "Description team 3");
    teams.add(team1);
    teams.add(team2);
    teams.add(team3);
    return new TeamResultWrapper(teams, teams.size(), 0, 10);
  }

  protected TeamResultWrapper getAllTeams() {
    List<Team> teams = new ArrayList<>();
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
    return new TeamResultWrapper(teams, teams.size(), 0, 10);
  }

  protected Returns getAllTeamReturn() {
    return new Returns(getAllTeams());
  }

  protected Team getTeam1() {
    Stem stem = new Stem("stem-1", "stem 1", "stem description");
    return new Team("team-1", "Team 1", "Nice description", stem, true);
  }

  protected Person getPerson1() {
    return getPerson("member-1");
  }

  protected Person getPerson(String id) {
    return new Person(id, "name", "email", "example.org", "admin", "John Doe");
  }

  protected List<Stem> getStems() {
    Stem stem1 = new Stem("stem-1", "stem 1", "stem description");
    Stem stem2 = new Stem("stem-2", "stem 2", "stem description");
    List<Stem> stems = new ArrayList<>();
    stems.add(stem1);
    stems.add(stem2);
    return stems;
  }

  protected Member getAdministrativeMember() {
    HashSet<Role> roles = new HashSet<>();
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

  protected TeamResultWrapper getSearchTeams() {
    ArrayList<Team> teams = new ArrayList<Team>();
    Team team1 = new Team("team-1", "Team 1", "Description team 1");
    teams.add(team1);
    return new TeamResultWrapper(teams, teams.size(), 0, 10);
  }

  /**
   * @param target         the controller
   * @param answer         the answer to return on method invocations
   * @param interfaceClass the class to mock
   */
  protected <T> void autoWireMock(Object target, Answer<?> answer, Class<T> interfaceClass) throws Exception {
    T mock = mock(interfaceClass, answer);
    autoWireMock(target, mock, interfaceClass);
  }

  /**
   * @param target         the controller
   * @param mock           the mock Object to return on method invocations
   * @param interfaceClass the class to mock
   */
  protected void autoWireMock(Object target, Object mock, Class<?> interfaceClass) throws Exception {
    boolean found = doAutoWireMock(target, mock, interfaceClass, target.getClass().getDeclaredFields());
    if (!found) {
      doAutoWireMock(target, mock, interfaceClass, target.getClass().getSuperclass().getDeclaredFields());
    }
  }

  private boolean doAutoWireMock(Object target, Object mock, Class<?> interfaceClass, Field[] fields) throws IllegalAccessException {
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

    Person person = getPerson1();
    session.setAttribute(LoginInterceptor.PERSON_SESSION_KEY, person);
    session.setAttribute(LoginInterceptor.USER_STATUS_SESSION_KEY, "member");

  }

  @Before
  public void setup() throws Exception {
    this.request = new MockHttpServletRequest();
    this.modelMap = new ModelMap();
    setUpSession(request);
  }

  protected MockHttpServletRequest getRequest() {
    return request;
  }

  protected ModelMap getModelMap() {
    return modelMap;
  }

}
