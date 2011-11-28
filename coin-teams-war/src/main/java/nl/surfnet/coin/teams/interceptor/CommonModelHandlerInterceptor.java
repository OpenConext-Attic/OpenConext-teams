package nl.surfnet.coin.teams.interceptor;

import nl.surfnet.coin.teams.util.TeamEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @version $Id$
 */
public class CommonModelHandlerInterceptor extends HandlerInterceptorAdapter {
  public static final String RPC_RELAY_URL = "shindigHost";
  private final TeamEnvironment teamEnvironment;

  @Autowired
  public CommonModelHandlerInterceptor(TeamEnvironment teamEnvironment) {
     this.teamEnvironment = teamEnvironment;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response,
                         Object handler, ModelAndView modelAndView) throws Exception {
    if (modelAndView != null) {
      ModelMap map = modelAndView.getModelMap();
      map.addAttribute(RPC_RELAY_URL, teamEnvironment.getShindigHost());
    }
    super.postHandle(request, response, handler, modelAndView);
  }
}
