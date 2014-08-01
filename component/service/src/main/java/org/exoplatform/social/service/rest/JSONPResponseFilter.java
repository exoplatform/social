package org.exoplatform.social.service.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.Filter;
import org.exoplatform.services.rest.GenericContainerResponse;
import org.exoplatform.services.rest.ResponseFilter;

@Filter
public class JSONPResponseFilter implements ResponseFilter {

  private HttpServletRequest httpRequest;
  
  public JSONPResponseFilter(@Context HttpServletRequest httpRequest) {
    this.httpRequest = httpRequest;
  }
  
  @Override
  public void doFilter(GenericContainerResponse response) {
    String callbackRequestParam = httpRequest.getParameter("jsonp");
    boolean isJSONPRequest = callbackRequestParam != null && callbackRequestParam.length() > 0;
    
    if (isJSONPRequest) {
      response.setResponse(Response
          .status(response.getStatus())
          .entity(response.getEntity())
          .type("text/javascript")
          .build());
    }
  }

}
