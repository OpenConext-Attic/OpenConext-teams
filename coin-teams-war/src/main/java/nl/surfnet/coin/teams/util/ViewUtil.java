package nl.surfnet.coin.teams.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;

/**
 * Util class to define which view should be used (gadget or app)
 */
public final class ViewUtil {
  private static final String VIEW = "view";

  private ViewUtil() {

  }

  /**
   * Defines which view must be presented
   *
   * @param request  current {@link HttpServletRequest}
   * @param modelMap {@link ModelMap} on which the view name is added
   */
  public static final void defineView(HttpServletRequest request, ModelMap modelMap) {
    String view = request.getParameter(VIEW);

    // Determine view
    if (!"gadget".equals(view)) {
      view = "app";
    }
    modelMap.addAttribute(VIEW, view);
  }
  
  public static final String getView(HttpServletRequest request) {
    String view = request.getParameter(VIEW);
    
    if (!"gadget".equals(view)) {
      view = "app";
    }
    
    return view;
  }
}
