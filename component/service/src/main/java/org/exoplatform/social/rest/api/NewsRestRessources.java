package org.exoplatform.social.rest.api;

import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public interface NewsRestRessources extends SocialRest {

  public Response clickOnNews(UriInfo uriInfo, String id, Map<String, String> targetField);
}
