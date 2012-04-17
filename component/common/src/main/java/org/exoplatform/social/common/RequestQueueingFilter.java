/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common;

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
    } catch (Exception e) {
      throw new ServletException(e.getCause());
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
      } catch (Exception e) {
       throw new InvocationException(e.getCause());
      }

      return null;
    }


    public HttpServletRequest getRequest() {
      return (HttpServletRequest) request;
    }

  }

}
