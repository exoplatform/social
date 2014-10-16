package org.exoplatform.social.service.rest.api;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.CharEncoding;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public abstract class AbstractSocialRestService {
  
  public static final int DEFAULT_LIMIT = 20;

  public static final int HARD_LIMIT    = 50;

  protected String getQueryParam(UriInfo uriInfo, String key) {
    URI uri = uriInfo.getRequestUri();
    if (uri.getQuery() == null) {
      return null;
    }
    List<NameValuePair> params = URLEncodedUtils.parse(uri, CharEncoding.UTF_8);
    for (NameValuePair param : params) {
      if (param.getName().equalsIgnoreCase(key)) {
        return param.getValue();
      }
    }

    return null;
  }
  
  protected String getQueryValueFields(UriInfo uriInfo) {
    return getQueryParam(uriInfo, "fields");
  }

  protected String getQueryValueExpand(UriInfo uriInfo) {
    return getQueryParam(uriInfo, "expand");
  }

  protected int getQueryValueLimit(UriInfo uriInfo) {
    String limit = getQueryParam(uriInfo, "limit");
    return (limit != null && Integer.valueOf(limit) > 0) ? Math.min(HARD_LIMIT, Integer.valueOf(limit)) : DEFAULT_LIMIT;
  }

  protected int getQueryValueOffset(UriInfo uriInfo) {
    String offset = getQueryParam(uriInfo, "offset");
    return (offset != null) ? Integer.valueOf(offset) : 0;
  }

  protected boolean getQueryValueReturnSize(UriInfo uriInfo) {
    return Boolean.valueOf(getQueryParam(uriInfo, "returnSize"));
  }
}
