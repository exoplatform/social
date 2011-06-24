/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.service.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.models.Activity;
import org.exoplatform.social.service.rest.api.models.ActivityStream;

import java.util.Arrays;

/**
 * Activity Resources end point.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 15, 2011
 */
@Path("api/social/" + VersionResources.LATEST_VERSION + "/{portalContainerName}/activity")
public class ActivityResources implements ResourceContainer {

  private static final String[] SUPPORTED_FORMAT = new String[]{"json"};

  /**
   * Gets an activity by its id.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity Id
   * @param format the expected returned format
   * @return a response object
   */
  @GET
  @Path("{activityId}.{format}")
  public Response getActivityById(@Context UriInfo uriInfo,
                                  @PathParam("portalContainerName") String portalContainerName,
                                  @PathParam("activityId") String activityId,
                                  @PathParam("format") String format,
                                  @QueryParam("posterIdentity") String showPosterIdentity,
                                  @QueryParam("numberOfComments") String numberOfComments,
                                  @QueryParam("activityStream") String showActivityStream) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    try {

      //
      ActivityManager manager = getActivityManager(portalContainerName);
      ExoSocialActivity activity = manager.getActivity(activityId);

      //
      Activity model = new Activity(activity);

      //
      if (isPassed(showPosterIdentity)) {
        model.setPosterIdentity(activity.getUserId());
      }

      //
      if (isPassed(showActivityStream)) {
        model.setActivityStream(new ActivityStream(
            "",
            activity.getStreamOwner(),
            activity.getStreamFaviconUrl(),
            activity.getStreamTitle(),
            activity.getStreamUrl()
        ));
      }

      //
      if (numberOfComments != null) {

        int commentNumber = activity.getReplyToId().length;
        int number = Integer.parseInt(numberOfComments);

        if (number > 100) {
          number = 100;
        }
        
        if (number > commentNumber) {
          number = commentNumber;
        }
        model.setComments(Arrays.asList(activity.getReplyToId()).subList(0, number).toArray(new String[]{}));
        model.setNumberOfComments(commentNumber);
      }

      return Util.getResponse(model, uriInfo, mediaType, Response.Status.OK);
    }
    catch (ActivityStorageException e) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.NOT_FOUND);
    }
    
  }


  /**
   * Creates a new activity.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param format the expected returned format
   * @param identityIdStream the optional identity stream to post this new activity to
   * @param newActivity a new activity instance
   * @return a response object
   */
  @POST
  @Path("new.{format}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createNewActivity(@Context UriInfo uriInfo,
                                    @PathParam("portalContainerName") String portalContainerName,
                                    @PathParam("format") String format,
                                    @QueryParam("identityId") String identityIdStream,
                                    Activity newActivity) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(newActivity.getTitle());
    activity.setUserId(identityIdStream);

    try {

      //
      ActivityManager activityManager = getActivityManager(portalContainerName);
      IdentityManager identityManager =  getIdentityManager(portalContainerName);

      //
      Identity identity = identityManager.getIdentity(identityIdStream, false);
      activityManager.saveActivityNoReturn(identity, activity);
      ExoSocialActivity got = activityManager.getActivity(activity.getId());

      //
      Activity model = new Activity(got);
      model.setIdentityId(identityIdStream);

      return Util.getResponse(model, uriInfo, mediaType, Response.Status.OK);
    }
    catch (Exception e) {
      e.printStackTrace();
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Deletes an existing activity by DELETE method from a specified activity id. Just returns the deleted activity
   * object.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @DELETE
  @Path("{activityId}.{format}")
  public Response deleteExistingActivityById(@Context UriInfo uriInfo,
                                            @PathParam("portalContainerName") String portalContainerName,
                                            @PathParam("activityId") String activityId,
                                            @PathParam("format") String format) {

    Response response = getActivityById(uriInfo, portalContainerName, activityId, format, null, null, null);

    if (response.getStatus() == Response.Status.OK.getStatusCode()) {
      ActivityManager manager = getActivityManager(portalContainerName);
      manager.deleteActivity(activityId);
    }

    return response;
  }

  /**
   * Deletes an existing activity by POST method from a specified activity id. Just returns the deleted activity
   * object. Deletes by DELETE method is recommended. This API should be used only when DELETE method is not supported
   * by the client.
   *
   * @param uriInfo the uri request uri
   * @param portalContainerName the associated portal container name
   * @param activityId the specified activity id
   * @param format the expected returned format
   * @return a response object
   */
  @POST
  @Path("destroy/{activityId}.{format}")
  public Response postToDeleteActivityById(@Context UriInfo uriInfo,
                                           @PathParam("portalContainerName") String portalContainerName,
                                           @PathParam("activityId") String activityId,
                                           @PathParam("format") String format) {

    return deleteExistingActivityById(uriInfo, portalContainerName, activityId, format);
    
  }

  private boolean isPassed(String value) {
    return value != null && ("true".equals(value) || "t".equals(value) || "1".equals(value));
  }

  private ActivityManager getActivityManager(String name) {
    return (ActivityManager) getPortalContainer(name).getComponentInstanceOfType(ActivityManager.class);
  }

  private IdentityManager getIdentityManager(String name) {
    return (IdentityManager) getPortalContainer(name).getComponentInstanceOfType(IdentityManager.class);
  }

  private PortalContainer getPortalContainer(String name) {
    return (PortalContainer) ExoContainerContext.getContainerByName(name);
  }
}
