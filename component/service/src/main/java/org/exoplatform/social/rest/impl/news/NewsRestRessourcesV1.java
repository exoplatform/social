package org.exoplatform.social.rest.impl.news;

import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.rest.api.NewsRestRessources;
import org.exoplatform.social.service.rest.api.VersionResources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path(VersionResources.VERSION_ONE + "/social/news")
@Api(tags = VersionResources.VERSION_ONE + "/social/news", value = VersionResources.VERSION_ONE
    + "/social/activities", description = "Managing news activities")
public class NewsRestRessourcesV1 implements NewsRestRessources {

  private static final Log LOG = ExoLogger.getLogger(NewsRestRessourcesV1.class);

  @POST
  @Path("{id}/click")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Click on read more or news title", httpMethod = "POST", response = Response.class, notes = "This will display a log message when the user click on read more or the title of a news")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Request fulfilled"),
      @ApiResponse(code = 500, message = "Internal server error"), @ApiResponse(code = 400, message = "Invalid query input") })
  public Response clickOnNews(@Context UriInfo uriInfo,
                              @ApiParam(value = "Activity id", required = true) @PathParam("id") String id,
                              @ApiParam(value = "The target cliked field", required = true) Map<String, String> targetField) {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class)
                                       .getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    ActivityStream activityStream = activity.getActivityStream();
    if ("news".equals(activity.getType()) && activityStream != null
        && activityStream.getType().equals(ActivityStream.Type.SPACE)) {
      LOG.info("service=news operation=click_on_{} parameters=\"activity_id:{},space_name:{},space_id:{},user_id:{}\"",
               targetField.get("name"),
               activity.getId(),
               activityStream.getPrettyId(),
               activityStream.getId(),
               currentUser.getId());
    }
    return Response.status(Response.Status.OK).build();
  }

}
