package nl.surfnet.coin.teams.util;

import java.util.List;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

public class SpringMvcConfiguration extends WebMvcConfigurerAdapter {

  private final List<HandlerInterceptor> handlerInterceptors;

  public SpringMvcConfiguration(List<HandlerInterceptor> handlerInterceptors) {
    this.handlerInterceptors = handlerInterceptors;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    for (HandlerInterceptor handlerInterceptor : handlerInterceptors) {
      registry.addInterceptor(handlerInterceptor);
    }
  }

}
