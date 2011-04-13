package nl.surfnet.coin.teams.util;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.springframework.ui.ModelMap;

/**
 * Test for {@link ViewUtil}
 */
public class ViewUtilTest {
  @Test
  public void testAddViewToModelMap() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);

    ModelMap modelMap = new ModelMap();

    when(request.getParameter("view")).thenReturn(null);
    ViewUtil.addViewToModelMap(request, modelMap);
    assertEquals("app", modelMap.get("view"));

    when(request.getParameter("view")).thenReturn("gadget");
    ViewUtil.addViewToModelMap(request, modelMap);
    assertEquals("gadget", modelMap.get("view"));

    when(request.getParameter("view")).thenReturn("app");
    ViewUtil.addViewToModelMap(request, modelMap);
    assertEquals("app", modelMap.get("view"));
  }

  @Test
  public void testGetView() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getParameter("view")).thenReturn(null);
    assertEquals("app", ViewUtil.getView(request));

    when(request.getParameter("view")).thenReturn("gadget");
    assertEquals("gadget", ViewUtil.getView(request));

    when(request.getParameter("view")).thenReturn("app");
    assertEquals("app", ViewUtil.getView(request));

  }
}
