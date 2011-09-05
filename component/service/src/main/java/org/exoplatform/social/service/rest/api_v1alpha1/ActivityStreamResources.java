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

package org.exoplatform.social.service.rest.api_v1alpha1;

import java.util.List;

import javax.ws.rs.GET;
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
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api_v1alpha1.models.Activity;
import org.exoplatform.social.service.rest.api_v1alpha1.models.ActivityList;

/**
 * Activity Stream Resources end point.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@Path("api/social/" + VersionResources.V1_ALPHA1 + "/{portalContainerName}/activity_stream")
public class ActivityStreamResources implements ResourceContainer {

  private static final String[] SUPPORTED_FORMAT = new String[]{"json"};

  /**
   * The the user activities.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/user/default.{format}")
  public Response getActivityStreamUserDefault(@Context UriInfo uriInfo,
                                               @PathParam("portalContainerName") String portalContainerName,
                                               @PathParam("identityId") String identityId,
                                               @PathParam("format") String format,
                                               @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadAsList(0, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the newer user activities.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param activityBaseId the activity base
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/user/newer/{activityBaseId}.{format}")
  public Response getActivityStreamUserNewer(@Context UriInfo uriInfo,
                                             @PathParam("portalContainerName") String portalContainerName,
                                             @PathParam("identityId") String identityId,
                                             @PathParam("activityBaseId") String activityBaseId,
                                             @PathParam("format") String format,
                                             @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);
    ExoSocialActivity activity = activityManager.getActivity(activityBaseId);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadNewer(activity, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the older user activities.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param activityBaseId the activity base
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/user/older/{activityBaseId}.{format}")
  public Response getActivityStreamUserOlder(@Context UriInfo uriInfo,
                                             @PathParam("portalContainerName") String portalContainerName,
                                             @PathParam("identityId") String identityId,
                                             @PathParam("activityBaseId") String activityBaseId,
                                             @PathParam("format") String format,
                                             @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);
    ExoSocialActivity activity = activityManager.getActivity(activityBaseId);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadOlder(activity, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the feed activities (user + space + connections).
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/feed/default.{format}")
  public Response getActivityStreamFeedDefault(@Context UriInfo uriInfo,
                                               @PathParam("portalContainerName") String portalContainerName,
                                               @PathParam("identityId") String identityId,
                                               @PathParam("format") String format,
                                               @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivityFeedWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadAsList(0, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the newer feed activities (user + space + connections).
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param activityBaseId the activity base
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/feed/newer/{activityBaseId}.{format}")
  public Response getActivityStreamFeedNewer(@Context UriInfo uriInfo,
                                             @PathParam("portalContainerName") String portalContainerName,
                                             @PathParam("identityId") String identityId,
                                             @PathParam("activityBaseId") String activityBaseId,
                                             @PathParam("format") String format,
                                             @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);
    ExoSocialActivity activity = activityManager.getActivity(activityBaseId);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivityFeedWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadNewer(activity, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the older feed activities (user + space + connections).
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param activityBaseId the activity base
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/feed/older/{activityBaseId}.{format}")
  public Response getActivityStreamFeedOlder(@Context UriInfo uriInfo,
                                             @PathParam("portalContainerName") String portalContainerName,
                                             @PathParam("identityId") String identityId,
                                             @PathParam("activityBaseId") String activityBaseId,
                                             @PathParam("format") String format,
                                             @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);
    ExoSocialActivity activity = activityManager.getActivity(activityBaseId);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivityFeedWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadOlder(activity, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the connections activities.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/connections/default.{format}")
  public Response getActivityStreamConnectionsDefault(@Context UriInfo uriInfo,
                                                      @PathParam("portalContainerName") String portalContainerName,
                                                      @PathParam("identityId") String identityId,
                                                      @PathParam("format") String format,
                                                      @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfConnectionsWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadAsList(0, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the newer connections activities.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param activityBaseId the activity base
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/connections/newer/{activityBaseId}.{format}")
  public Response getActivityStreamConnectionsNewer(@Context UriInfo uriInfo,
                                                    @PathParam("portalContainerName") String portalContainerName,
                                                    @PathParam("identityId") String identityId,
                                                    @PathParam("activityBaseId") String activityBaseId,
                                                    @PathParam("format") String format,
                                                    @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);
    ExoSocialActivity activity = activityManager.getActivity(activityBaseId);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfConnectionsWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadNewer(activity, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the older connections activities.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param activityBaseId the activity base
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/connections/older/{activityBaseId}.{format}")
  public Response getActivityStreamConnectionsOlder(@Context UriInfo uriInfo,
                                                    @PathParam("portalContainerName") String portalContainerName,
                                                    @PathParam("identityId") String identityId,
                                                    @PathParam("activityBaseId") String activityBaseId,
                                                    @PathParam("format") String format,
                                                    @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);
    ExoSocialActivity activity = activityManager.getActivity(activityBaseId);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfConnectionsWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadOlder(activity, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the space activities.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/spaces/default.{format}")
  public Response getActivityStreamSpacesDefault(@Context UriInfo uriInfo,
                                                      @PathParam("portalContainerName") String portalContainerName,
                                                      @PathParam("identityId") String identityId,
                                                      @PathParam("format") String format,
                                                      @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfUserSpacesWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadAsList(0, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the newer space activities.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param activityBaseId the activity base
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/spaces/newer/{activityBaseId}.{format}")
  public Response getActivityStreamSpacesNewer(@Context UriInfo uriInfo,
                                                    @PathParam("portalContainerName") String portalContainerName,
                                                    @PathParam("identityId") String identityId,
                                                    @PathParam("activityBaseId") String activityBaseId,
                                                    @PathParam("format") String format,
                                                    @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);
    ExoSocialActivity activity = activityManager.getActivity(activityBaseId);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfUserSpacesWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadNewer(activity, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * The the older space activities.
   *
   * @param uriInfo the uri request info
   * @param portalContainerName the associated portal container name
   * @param identityId the identity id
   * @param activityBaseId the activity base
   * @param format the format
   * @param limit the limit (optional)
   * @return a response object
   */
  @GET
  @Path("{identityId}/spaces/older/{activityBaseId}.{format}")
  public Response getActivityStreamSpacesOlder(@Context UriInfo uriInfo,
                                                    @PathParam("portalContainerName") String portalContainerName,
                                                    @PathParam("identityId") String identityId,
                                                    @PathParam("activityBaseId") String activityBaseId,
                                                    @PathParam("format") String format,
                                                    @QueryParam("limit") String limit) {

    MediaType mediaType = Util.getMediaType(format, SUPPORTED_FORMAT);

    if (!authenticated()) {
      return Util.getResponse(null, uriInfo, mediaType, Response.Status.UNAUTHORIZED);
    }

    //
    ActivityManager activityManager = getActivityManager(portalContainerName);
    IdentityManager identityManager = getIdentityManager(portalContainerName);

    //
    Identity identity = identityManager.getIdentity(identityId, false);
    ExoSocialActivity activity = activityManager.getActivity(activityBaseId);

    //
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfUserSpacesWithListAccess(identity);
    List<ExoSocialActivity> activities = listAccess.loadOlder(activity, secureLimit(limit));

    //
    return Util.getResponse(build(activities), uriInfo, mediaType, Response.Status.OK);

  }

  /**
   * Secure the limit access : return 100 if the limit is null.
   *
   * @param limit the limit
   * @return the limit.
   */
  private int secureLimit(String limit) {
    if (limit == null) {
      return 100;
    }

    int intLimit = Integer.parseInt(limit);
    if (intLimit > 100) {
      return 100;
    }
    else {
      return intLimit;
    }
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

  /**
   * Build an ActivityList from List<ExoSocialActivity>.
   * @param activities the data
   * @return the list
   */
  private ActivityList build(List<ExoSocialActivity> activities) {
    ActivityList activityList = new ActivityList();

    for (ExoSocialActivity current : activities) {
      activityList.addActivity(new Activity(current));
    }
    
    return activityList;
  }

  private boolean authenticated() {

    return ConversationState.getCurrent() != null;
  }
}
