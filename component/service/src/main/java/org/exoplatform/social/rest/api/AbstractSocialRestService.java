package org.exoplatform.social.rest.api;


import org.exoplatform.services.rest.impl.ApplicationContextImpl;

public abstract class AbstractSocialRestService {
  
  public static final int DEFAULT_LIMIT = 20;

  public static final int HARD_LIMIT    = 50;

  protected String getPathParam(String name) {
    return ApplicationContextImpl.getCurrent().getPathParameters().getFirst(name);
  }

  protected String getQueryParam(String name) {
    return ApplicationContextImpl.getCurrent().getQueryParameters().getFirst(name);   
  }
  
  protected int getQueryValueLimit() {
    Integer limit = getIntegerValue("limit");
    return (limit != null && limit > 0) ? Math.min(HARD_LIMIT, limit) : DEFAULT_LIMIT;
  }

  protected int getQueryValueOffset() {
    Integer offset = getIntegerValue("offset");
    return (offset != null) ? offset : 0;
  }
  
  protected Integer getIntegerValue(String name) {
    String value = getQueryParam(name);
    if (value == null)
      return null;
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  protected boolean getQueryValueReturnSize() {
    return Boolean.parseBoolean(getQueryParam("returnSize"));
  }
}
