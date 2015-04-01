package nl.surfnet.coin.teams.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class CommonModelHandlerInterceptor extends HandlerInterceptorAdapter {

  public static final String RPC_RELAY_URL = "shindigHost";
  private final String shindigHost;

  public CommonModelHandlerInterceptor(String shindigHost) {
    this.shindigHost = shindigHost;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response,
                         Object handler, ModelAndView modelAndView) throws Exception {
    if (modelAndView != null) {
      ModelMap map = modelAndView.getModelMap();
      map.addAttribute(RPC_RELAY_URL, shindigHost);
    }
    super.postHandle(request, response, handler, modelAndView);
  }
}
