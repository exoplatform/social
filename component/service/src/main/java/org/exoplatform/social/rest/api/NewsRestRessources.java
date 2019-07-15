package org.exoplatform.social.rest.api;

import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public interface NewsRestRessources extends SocialRest {

  @POST
  @Path("{id}/click")
  public Response clickOnNews(@Context UriInfo uriInfo, @PathParam("id") String id, Map<String, String> tragetField);
}
