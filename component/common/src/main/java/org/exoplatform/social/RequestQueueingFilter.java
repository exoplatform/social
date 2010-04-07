package org.exoplatform.social;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.web.filter.Filter;
import org.gatein.pc.api.invocation.InvocationException;

public class RequestQueueingFilter  implements Filter {

  private SessionLockInterceptor interceptor = new SessionLockInterceptor();

 

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
  ServletException {
    try {
      interceptor.invoke(new ServletFilterInvocation(chain, request, response));
    } catch (Throwable t) {
      throw new ServletException(t.getCause());
    }
  }



  public class SessionLockInterceptor extends LockInterceptor<ServletFilterInvocation> {

    @Override
    protected Object getLockId(ServletFilterInvocation invocation) throws InvocationException {
      //
      HttpServletRequest req = invocation.getRequest();

      // We lock only if the client provides a session id
      return req.getRequestedSessionId();
    }

  }
  
  public class ServletFilterInvocation implements Invocation {
    
    private FilterChain filterChain;
    private ServletRequest request;
    private ServletResponse response;


    public ServletFilterInvocation(FilterChain filterChain, ServletRequest request, ServletResponse response) {
      this.filterChain = filterChain;
      this.request = request;
      this.response = response;
    }
    

    public Object invokeNext() throws InvocationException {
      
      try {
        filterChain.doFilter(request, response);
      } catch (Throwable t) {
       throw new InvocationException(t.getCause());
      }

      return null;
    }


    public HttpServletRequest getRequest() {
      return (HttpServletRequest) request;
    }

  }

}
