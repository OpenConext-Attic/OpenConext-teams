package nl.surfnet.coin.teams.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class PolicyFilter implements Filter {

  @Override
  public void destroy() {
    // no destroy needed
    
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException {
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;
    httpServletResponse.addHeader("P3P", "CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT NOI\"");
    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig arg0) throws ServletException {
    // no init needed
  }

}