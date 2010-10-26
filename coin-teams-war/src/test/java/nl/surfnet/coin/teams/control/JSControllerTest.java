/**
 * 
 */
package nl.surfnet.coin.teams.control;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author steinwelberg
 *
 */
public class JSControllerTest extends AbstractControllerTest {
  
  private JSController jsController = new JSController();
  
  @Test
  public void testJS() {
    String result = jsController.js(getModelMap(), getRequest());
    assertEquals("js", result);
  }

}
