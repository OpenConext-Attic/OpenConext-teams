package nl.surfnet.coin.teams.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.ModelMap;

/**
 * Util class to define which view should be used (gadget or app)
 */
public final class ViewUtil {
  private static final String VIEW = "view";

  private ViewUtil() {

  }

  /**
   * Defines which view must be presented and adds it to the ModelMap
   *
   * @param request  current {@link HttpServletRequest}
   * @param modelMap {@link ModelMap} on which the view name is added
   */
  public static final void addViewToModelMap(HttpServletRequest request,
                                             ModelMap modelMap) {
    modelMap.addAttribute(VIEW, getView(request));
  }

  /**
   * Defines which view must be presented
   *
   * @param request current {@link HttpServletRequest}
   * @return the name of the view
   */
  public static final String getView(HttpServletRequest request) {
    String view = request.getParameter(VIEW);

    if (!"gadget".equals(view)) {
      view = "app";
    }

    return view;
  }
}
