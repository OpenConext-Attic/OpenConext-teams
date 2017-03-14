package teams.voot;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import teams.AbstractApplicationTest;
import teams.interceptor.LoginInterceptor;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.*;

public class VootApiControllerTest extends AbstractApplicationTest {

  private String contextPath = "/" + LoginInterceptor.API_VOOT_URL;

  @Value("${voot.api.user}")
  protected String user;

  @Value("${voot.api.password}")
  protected String password;

  @Test
  public void findByLocalGroupId() throws Exception {
    start("group/nl:surfnet:diensten:giants")
      .body("displayName", equalTo("giants"));
  }

  @Test
  public void linkedLocalTeamsGroup() throws Exception {
    String[] params = {"externalGroupIds","urn:collab:group:example.org:name1,urn:collab:group:example.org:name2"};
    start("linked-locals", Optional.of(params))
      .body("size()", equalTo(2))
      .body("displayName", hasItems("riders","giants"));
  }

  @Test
  public void linkedExternalGroupIds() throws Exception {
    String[] params = {"teamId","nl:surfnet:diensten:riders"};
    start("linked-externals", Optional.of(params))
      .body("size()", equalTo(2))
      .body("", hasItems("urn:collab:group:example.org:name1","urn:collab:group:example.org:name2"));
  }

  @Test
  public void getMembers() throws Exception {
    start("members/nl:surfnet:diensten:giants")
      .body("size()", equalTo(4))
      .body("name", hasItems("Tracey Doe","Mary Doe","John Doe","William Doe"));
  }

  @Test
  public void getAllGroups() throws Exception {
    start("groups")
      .body("size()", equalTo(3))
      .body("displayName", hasItems("riders","giants","gliders"));
  }

  private ValidatableResponse start(String path) {
    return start(path, Optional.empty());
  }

  private ValidatableResponse start(String path, Optional<String[]> paramsOptional) {
    RequestSpecification specification = given()
      .auth().preemptive().basic(user, password);
    paramsOptional.ifPresent(params -> specification.queryParam(params[0], params[1]));
    return specification
      .when()
      .get(contextPath + "/" + path)
      .then()
      .statusCode(SC_OK);
  }

}
