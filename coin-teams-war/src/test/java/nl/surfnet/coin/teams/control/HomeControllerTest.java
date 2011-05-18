/**
 * 
 */
package nl.surfnet.coin.teams.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;

import nl.surfnet.coin.teams.domain.Team;
import nl.surfnet.coin.teams.service.TeamService;
import nl.surfnet.coin.teams.util.TeamEnvironment;

/**
 * @author steinwelberg
 *
 */
public class HomeControllerTest extends AbstractControllerTest {
  
  private HomeController homeController = new HomeController();
  
  @Test
  public void testStartMyTeams() throws Exception {
    MockHttpServletRequest request = getRequest();
    // This requests my teams
    request.setParameter("teams", "my");
    request.setParameter("teamSearch", "query");
    
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH) ,LocaleResolver.class);
    autoWireMock(homeController, getMyTeamReturn(), TeamService.class);
    
    homeController.start(getModelMap(), request);
    @SuppressWarnings("unchecked")
    ArrayList<Team> teams = (ArrayList<Team>) getModelMap().get("teams");
    String display = (String) getModelMap().get("display");
    
    assertEquals(3, teams.size());
    assertEquals("my", display);
  }
  
  @Test
  public void testStartAllTeams() throws Exception {
    MockHttpServletRequest request = getRequest();
    // This requests my teams
    request.setParameter("teams", "all");
    
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH) ,LocaleResolver.class);
    autoWireMock(homeController, getAllTeamReturn(), TeamService.class);
    
    homeController.start(getModelMap(), request);
    @SuppressWarnings("unchecked")
    ArrayList<Team> teams = (ArrayList<Team>) getModelMap().get("teams");
    String display = (String) getModelMap().get("display");
    String query = (String) getModelMap().get("query");
    
    assertEquals(6, teams.size());
    assertEquals("all", display);
    assertNull(query);
  }
  
  @Test
  public void testStartSearchMyTeams() throws Exception {
    MockHttpServletRequest request = getRequest();
    // This requests my teams
    request.setParameter("teams", "my");
    request.setParameter("teamSearch", "1");
    
    autoWireMock(homeController, new Returns("query"), MessageSource.class);
    autoWireMock(homeController, new Returns(Locale.ENGLISH) ,LocaleResolver.class);
    autoWireMock(homeController, getSearchTeamReturn(), TeamService.class);
    
    homeController.start(getModelMap(), request);
    @SuppressWarnings("unchecked")
    ArrayList<Team> teams = (ArrayList<Team>) getModelMap().get("teams");
    String display = (String) getModelMap().get("display");
    String query = (String) getModelMap().get("query");
    
    assertEquals(1, teams.size());
    assertEquals("my", display);
    assertEquals("1", query);
  }

  @Override
  public void setup() throws Exception {
    super.setup();
    TeamEnvironment mockTeamEnvironment = mock(TeamEnvironment.class);
    mockTeamEnvironment.setDefaultStemName("nl:surfnet:diensten");
    homeController.setTeamEnvironment(mockTeamEnvironment);
  }
}
