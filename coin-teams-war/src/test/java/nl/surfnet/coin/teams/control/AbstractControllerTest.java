/**
 * Copyright 2010
 */
package nl.surfnet.coin.teams.control;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.interceptor.LoginInterceptor;

/**
 * Base class for testing {@link Controller} instances
 * 
 */
public abstract class AbstractControllerTest {

  private MockHttpServletRequest request;
  private ModelMap modelMap;

  /**
   * Autowire all dependencies with a annotation autowired with a mock that does
   * nothing
   * 
   * @param target
   *          the controller
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
    ArrayList<Team> teams = new ArrayList<Team>();
    Team team1 = new Team("team-1", "Team 1", "Description team 1");
    Team team2 = new Team("team-2", "Team 2", "Description team 2");
    Team team3 = new Team("team-3", "Team 3", "Description team 3");
    teams.add(team1);
    teams.add(team2);
    teams.add(team3);
    return new Returns(teams);
  }
  
  protected Returns getAllTeamReturn() {
    ArrayList<Team> teams = new ArrayList<Team>();
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
    return new Returns(teams);
  }
  
  protected Returns getSearchTeamReturn() {
    ArrayList<Team> teams = new ArrayList<Team>();
    Team team1 = new Team("team-1", "Team 1", "Description team 1");
    teams.add(team1);
    return new Returns(teams);
  }

  /**
   * 
   * @param target
   *          the controller
   * @param answer
   *          the answer to return on method invocations
   * @param interfaceClass
   *          the class to mock
   */
  @SuppressWarnings("unchecked")
  protected void autoWireMock(Object target, Answer answer, Class interfaceClass)
      throws Exception {
    Object mock = mock(interfaceClass, answer);
    autoWireMock(target, mock, interfaceClass);
  }

  /**
   * 
   * @param target
   *          the controller
   * @param mock
   *          the mock Object to return on method invocations
   * @param interfaceClass
   *          the class to mock
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
   * @param request
   *          the HttpServletRequest
   */
  private void setUpSession(HttpServletRequest request) {
    HttpSession session = request.getSession(true);

    Person person = new Person();
    person.setField("id","member-1");
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
