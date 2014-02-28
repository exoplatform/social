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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.service.rest.RestChecker;
import org.exoplatform.social.service.rest.SecurityManager;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.models.ActivityRestListOut;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

import static org.exoplatform.social.service.rest.RestChecker.*;

/**
 * Provides API to access the activity stream of an identity.
 *
 * @anchor ActivityStreamResources
 *
 * @since 1.2.3
 */
@Path("api/social/" + VersionResources.LATEST_VERSION + "/{portalContainerName}/activity_stream/")
public class ActivityStreamResources implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(ActivityStreamResources.class.getName());

  private static final String[] SUPPORTED_FORMATS = new String[] {"json"};
  private static final int MAX_LIMIT = 100;


  /**
   * Gets activities of a defined identity based on an specific activity called baseActivity.
   *
   * @param uriInfo             The URI information.
   * @param portalContainerName The portal container name.
   * @param identityId          The identity Id.
   *                            There is one special *identityId*: "me" standing for the authenticated user who makes this request.
   * @param format              The format of the returned result, for example, JSON, or XML.
   * @param limit               The number of activities retrieved with the default value of 100. This input value must
   *                            be less than or equal to its default value (100). The number of the returned results is
   *                            actually less than or equal to the *limit* value.
   *                            If it is not specified, the default value is 100.
   * @param sinceId             Returns the activities having the created timestamps greater than the specified
   *                            *since\_Id*'s created timestamp.
   * @param maxId               Returns the activities having the created timestamps less than the specified *max\_id*'s
   *                            created timestamp. Note that *since\_id* and *max\_id* must not be defined in one
   *                            request, if they are, the *since\_id* query param is chosen.
   * @param numberOfComments    Specifies the number of latest comments to be displayed along with each activity.
   *                            By default, *number\_of\_comments=0*. If *number\_of\_comments* is a positive number,
   *                            this number is considered as a limit number that must be equal or less than 100. If the
   *                            total number of comments is less than the provided positive number, the number of actual
   *                            comments must be returned. If the total number of comments is more than 100, it is
   *                            recommended to use *activity/\:activityId/comments.format* instead.
   * @param numberOfLikes       Specifies the number of latest detailed likes to be returned along with this activity.
   *                            By default, *number\_of\_likes=0*. If *number\_of\_likes* is a positive number, this
   *                            number is considered as a limit number that must be equal or less than 100. If the total
   *                            number of likes is less than the provided positive number, the number of actual likes
   *                            must be returned. If the total number of likes is more than 100, it is recommended to
   *                            use *activity/\:activityId/likes.format* instead.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity_stream/f92cd6f0c0a80137102696ac26430766.json?limit=30&since_id=12345&number_of_likes=5
   * @response
   * {
   * "activities":[
   * {
   *       "id":"1a2b3c4d5e6f7g8h9j",
   *       "title":"Hello World!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 17 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId": "",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   * },
   * {
   *       "id":"1a210983123f7g8h9j",
   *       "title":"Hello World 1!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 19 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     }
   *   ]
   * }
   * @return the response
   * @LevelAPI Platform
   * @anchor ActivityStreamResources.getActivityStreamByIdentityId
   *
   */
  @GET
  @Path("{identityId}.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActivityStreamByIdentityId(@Context UriInfo uriInfo,
                                                @PathParam("portalContainerName") String portalContainerName,
                                                @PathParam("identityId") String identityId,
                                                @PathParam("format") String format,
                                                @QueryParam("limit") int limit,
                                                @QueryParam("since_id") String sinceId,
                                                @QueryParam("max_id") String maxId,
                                                @QueryParam("number_of_comments") int numberOfComments,
                                                @QueryParam("number_of_likes") int numberOfLikes) {
    checkAuthenticatedRequest();
    PortalContainer portalContainer = checkValidPortalContainerName(portalContainerName);
    MediaType mediaType = checkSupportedFormat(format, SUPPORTED_FORMATS);
    Identity targetIdentity;
    if ("me".equals(identityId)) {
      targetIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    } else {
      targetIdentity = Util.getIdentityManager(portalContainerName).getIdentity(identityId, false);
    }

    if (targetIdentity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    //check permission
    boolean canAccess = SecurityManager.canAccessActivityStream(portalContainer,
                                                                Util.getAuthenticatedUserIdentity(portalContainerName),
                                                                targetIdentity);
    if (!canAccess) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    int maxLimit = limit <= 0 ? MAX_LIMIT : Math.min(limit, MAX_LIMIT);
    ExoSocialActivity baseActivity = null;
    boolean getOlder = false;
    //if sinceId and maxId is both passed, sinceId is chosen
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    try {
      if (sinceId != null) {
        baseActivity = activityManager.getActivity(sinceId);
        if (baseActivity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      } else if (maxId != null) {
        getOlder = true;
        baseActivity = activityManager.getActivity(maxId);
        if (baseActivity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      }
    } catch (UndeclaredThrowableException udte) { //bad thing from cache service that we have to handle like this :(
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }

    RealtimeListAccess<ExoSocialActivity> rala = activityManager.getActivitiesWithListAccess(targetIdentity);
    List<ExoSocialActivity> activityList;
    if (getOlder) {
      activityList = rala.loadOlder(baseActivity, maxLimit);
    } else if (sinceId != null) {
      activityList = rala.loadNewer(baseActivity, maxLimit);
    } else {
      activityList = rala.loadAsList(0, maxLimit);
    }

    ActivityRestListOut arlo = new ActivityRestListOut(activityList, numberOfComments,
                                                       numberOfLikes, portalContainerName);

    return Util.getResponse(arlo, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Gets the activity stream feed of the authenticated user identity based on an specific activity called "baseActivity".
   *
   * @param uriInfo             The URI information.
   * @param portalContainerName The portal container name.
   * @param format              The format of the returned result, for example, JSON, or XML.
   * @param limit               Specifies the number of activities to retrieve. It must be less than or equal to 100.
   *                            The value you pass as limit is a maximum number of activities to be returned.
   *                            The actual number of activities you receive maybe less than limit.
   *                            If it is not specified, the default value is 100.
   * @param sinceId             Returns the activities having the created timestamps greater than
   *                            the specified *since\_id*'s created timestamp.
   * @param maxId               Returns the activities having the created timestamp less than the specified *max\_id*'s created
   *                            timestamp. Note that *since\_id* and *max\_id* must not be defined in one request,
   *                            if they are defined, the *since\_id* query param is chosen.
   * @param numberOfComments    Specifies the latest number of comments to be displayed along with each activity.
   *                            By default, *number\_of\_comments=0*. If *number\_of\_comments* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number of
   *                            comments is less than the provided positive number, the number of actual comments must be
   *                            returned. If the total number of comments is more than 100,
   *                            it is recommended to use: "*activity/\:activityId/comments.format*" instead.
   * @param numberOfLikes       Specifies the latest number of detailed likes to be returned along with this activity.
   *                            By default, *number\_of\_likes=0*. If *number\_of\_likes* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number
   *                            of likes is less than the provided positive number, the number of actual likes must be
   *                            returned. If the total number of likes is more than 100, it is recommended to use:
   *                            "*activity/\:activityId/likes.format*" instead.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity_stream/feed.json?limit=30&since_id=12345&number_of_comments=5&number_of_likes=5
   * @response
   * {
   *   "activities":[
   *     {
   *       "id":"1a2b3c4d5e6f7g8h9j",
   *       "title":"Hello World!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 17 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     },
   *     {
   *       "id":"1a210983123f7g8h9j",
   *       "title":"Hello World 1!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 19 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     }
   *   ]
   * }
   * @return the response
   * @LevelAPI Platform
   * @anchor ActivityStreamResources.getActivityFeedOfAuthenticated
   *
   */
  @GET
  @Path("feed.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActivityFeedOfAuthenticated(@Context UriInfo uriInfo,
                                                 @PathParam("portalContainerName") String portalContainerName,
                                                 @PathParam("format") String format,
                                                 @QueryParam("limit") int limit,
                                                 @QueryParam("since_id") String sinceId,
                                                 @QueryParam("max_id") String maxId,
                                                 @QueryParam("number_of_comments") int numberOfComments,
                                                 @QueryParam("number_of_likes") int numberOfLikes) {
    checkAuthenticatedRequest();
    checkValidPortalContainerName(portalContainerName);
    MediaType mediaType = checkSupportedFormat(format, SUPPORTED_FORMATS);

    Identity sourceIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);

    int maxLimit = limit <= 0 ? MAX_LIMIT : Math.min(limit, MAX_LIMIT);

    ActivityManager activityManager = Util.getActivityManager(portalContainerName);

    ExoSocialActivity newerActivity = null;
    ExoSocialActivity olderActivity = null;

    try {
      if (sinceId != null && !sinceId.trim().equals("")) {
        newerActivity = activityManager.getActivity(sinceId);
        if (newerActivity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      } else if (maxId != null && !maxId.trim().equals("")) {
        olderActivity = activityManager.getActivity(maxId);
        if (olderActivity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      }
    } catch (UndeclaredThrowableException e) {
      if (e.getCause() instanceof ActivityStorageException) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }

    RealtimeListAccess<ExoSocialActivity> listAccess = null;
    List<ExoSocialActivity> activities = null;

    try {
      listAccess = activityManager.getActivityFeedWithListAccess(sourceIdentity);
      if (newerActivity != null) {
        activities = listAccess.loadNewer(newerActivity, maxLimit);
      } else if (olderActivity != null) {
        activities = listAccess.loadOlder(olderActivity, maxLimit);
      } else {
        activities = listAccess.loadAsList(0, maxLimit);
      }
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    ActivityRestListOut activityRestListOut = new ActivityRestListOut(activities, numberOfComments,
                                                       numberOfLikes, portalContainerName);
    return Util.getResponse(activityRestListOut, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Gets space activities of spaces based on an specific activity called "baseActivity".
   *
   * @param uriInfo             The URI information.
   * @param portalContainerName The portal container name.
   * @param format              The format of the returned result, for example, JSON, or XML.
   * @param limit               Specifies the number of activities to retrieve. It must be less than or equal to 100.
   *                            The value you pass as limit is a maximum number of activities to be returned.
   *                            The actual number of activities you receive maybe less than limit.
   *                            If it is not specified, the default value will be 100.
   * @param sinceId             Returns the activities having the created timestamps greater than
   *                            the specified *since\_id*'s created timestamp.
   * @param maxId               Returns the activities having the created timestamp less than the specified *max\_id*'s created
   *                            timestamp. Note that *since\_id* and *max\_id* must not be defined in one request,
   *                            if they are defined, the *since\_id* query param is chosen.
   * @param numberOfComments    Specifies the latest number of comments to be displayed along with each activity.
   *                            By default, *number\_of\_comments=0*. If *number\_of\_comments* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number of
   *                            comments is less than the provided positive number, the number of actual comments must be
   *                            returned. If the total number of comments is more than 100,
   *                            it is recommended to use "*activity/\:activityId/comments.format*" instead.
   * @param numberOfLikes       Specifies the latest number of detailed likes to be returned along with this activity.
   *                            By default, *number\_of\_likes=0*. If *number\_of\_likes* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number
   *                            of likes is less than the provided positive number, the number of actual likes must be
   *                            returned. If the total number of likes is more than 100, it is recommended to use
   *                            "*activity/\:activityId/likes.format*" instead.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity_stream/spaces.json?limit=30&since_id=12345&number_of_comments=5&number_of_likes=5
   * @response
   * {
   *   "activities":[
   *     {
   *       "id":"1a2b3c4d5e6f7g8h9j",
   *       "title":"Hello World!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 17 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     },
   *     {
   *       "id":"1a210983123f7g8h9j",
   *       "title":"Hello World 1!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 19 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     }
   *   ]
   * }
   * @return the response
   * @LevelAPI Platform
   * @anchor ActivityStreamResources.getActivitySpacesOfAuthenticated
   *
   */
  @GET
  @Path("spaces.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActivitySpacesOfAuthenticated(@Context UriInfo uriInfo,
                                                   @PathParam("portalContainerName") String portalContainerName,
                                                   @PathParam("format") String format,
                                                   @QueryParam("limit") int limit,
                                                   @QueryParam("since_id") String sinceId,
                                                   @QueryParam("max_id") String maxId,
                                                   @QueryParam("number_of_comments") int numberOfComments,
                                                   @QueryParam("number_of_likes") int numberOfLikes) {
    checkAuthenticatedRequest();
    checkValidPortalContainerName(portalContainerName);
    MediaType mediaType = checkSupportedFormat(format, SUPPORTED_FORMATS);

    Identity targetIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);

    int maxLimit = limit == 0 ? MAX_LIMIT : limit;
    ExoSocialActivity baseActivity = null;
    boolean getOlder = false;
    //if sinceId and maxId is both passed, sinceId is chosen
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    try {
      if (sinceId != null) {
        baseActivity = activityManager.getActivity(sinceId);
        
        if (baseActivity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
      } else if (maxId != null) {
        getOlder = true;
        baseActivity = activityManager.getActivity(maxId);
        
        if (baseActivity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      }
    } catch (UndeclaredThrowableException udte) { //bad thing from cache service that we have to handle like this :(
      if (udte.getCause() instanceof ActivityStorageException) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }

    RealtimeListAccess<ExoSocialActivity> rala = activityManager.getActivitiesOfUserSpacesWithListAccess(targetIdentity);
    List<ExoSocialActivity> activityList;
    if (getOlder) {
      activityList = rala.loadOlder(baseActivity, maxLimit);
    } else if (sinceId != null) {
      activityList = rala.loadNewer(baseActivity, maxLimit);
    } else {
      activityList = rala.loadAsList(0, maxLimit);
    }

   ActivityRestListOut arlo = new ActivityRestListOut(activityList, numberOfComments, numberOfLikes, portalContainerName);

   return Util.getResponse(arlo, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Gets activities of connections of a specified identity based on an specific activity called "baseActivity".
   *
   * @param uriInfo             The URI information.
   * @param portalContainerName The portal container name.
   * @param format              The response format type, for example: JSON, or XML.
   * @param limit               Specifies the number of activities to retrieve. It must be less than or equal to 100.
   *                            The value you pass as limit is a maximum number of activities to be returned.
   *                            The actual number of activities you receive maybe less than limit.
   *                            If it is not specified, the default value will be 100.
   * @param sinceId             Returns the activities having the created timestamps greater than
   *                            the specified *since\_id*'s created timestamp.
   * @param maxId               Returns the activities having the created timestamp less than the specified *max\_id*'s created
   *                            timestamp. Note that *since\_id* and *max\_id* must not be defined in one request,
   *                            if they are defined, the *since\_id* query param is chosen.
   * @param numberOfComments    Specifies the latest number of comments to be displayed along with each activity.
   *                            By default, *number\_of\_comments=0*. If *number\_of\_comments* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number of
   *                            comments is less than the provided positive number, the number of actual comments must be
   *                            returned. If the total number of comments is more than 100,
   *                            it is recommended you use "*activity/\:activityId/comments.format*" instead.
   * @param numberOfLikes       Specifies the latest number of detailed likes to be returned along with this activity.
   *                            By default, *number\_of\_likes=0*. If *number\_of\_likes* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number
   *                            of likes is less than the provided positive number, the number of actual likes must be
   *                            returned. If the total number of likes is more than 100, it is recommended to use:
   *                            "*activity/\:activityId/likes.format*" instead.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity_stream/connections.json?limit=30&since_id=12345&number_of_comments=5&number_of_likes=5
   * @response
   * {
   *   "activities":[
   *     {
   *       "id":"1a2b3c4d5e6f7g8h9j",
   *       "title":"Hello World!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 17 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     },
   *     {
   *       "id":"1a210983123f7g8h9j",
   *       "title":"Hello World 1!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 19 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     }
   *   ]
   * }
   * @return the response
   * @LevelAPI Platform
   * @anchor ActivityStreamResources.getActivityConnectionsOfAuthenticated
   *
   */
  @GET
  @Path("connections.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActivityConnectionsOfAuthenticated(@Context UriInfo uriInfo,
                                                        @PathParam("portalContainerName") String portalContainerName,
                                                        @PathParam("format") String format,
                                                        @QueryParam("limit") int limit,
                                                        @QueryParam("since_id") String sinceId,
                                                        @QueryParam("max_id") String maxId,
                                                        @QueryParam("number_of_comments") int numberOfComments,
                                                        @QueryParam("number_of_likes") int numberOfLikes) {

    RestChecker.checkAuthenticatedRequest();
    RestChecker.checkValidPortalContainerName(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMATS);

    Identity targetIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);

    ExoSocialActivity baseActivity = null;
    boolean getOlder = false;

    // sinceId and maxId. If both of sinceId and maxId is added then sinceId is chosen.
    try {
      if (sinceId != null) {
        baseActivity = activityManager.getActivity(sinceId);
        
        if (baseActivity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        
      } else if (maxId != null) {
        getOlder = true;
        baseActivity = activityManager.getActivity(maxId);
        
        if (baseActivity == null) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      }
    } catch (UndeclaredThrowableException udte) {
      if (udte.getCause() instanceof ActivityStorageException) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else {
        throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
      }
    }

    RealtimeListAccess<ExoSocialActivity> realtimeListAccess = null;

    realtimeListAccess = activityManager.getActivitiesOfConnectionsWithListAccess(targetIdentity);

    List<ExoSocialActivity> activityList;
    int maxLimit = limit <= 0 ? MAX_LIMIT : Math.min(limit, MAX_LIMIT);

    if (getOlder) {
      activityList = realtimeListAccess.loadOlder(baseActivity, maxLimit);
    } else if (sinceId != null) {
      activityList = realtimeListAccess.loadNewer(baseActivity, maxLimit);
    } else {
      activityList = realtimeListAccess.loadAsList(0, maxLimit);
    }

    ActivityRestListOut activityRestListOut = new ActivityRestListOut(activityList, numberOfComments,
            numberOfLikes, portalContainerName);

    return Util.getResponse(activityRestListOut, uriInfo, mediaType, Response.Status.OK);
  }

  /**
   * Gets activities of a defined identity based on a specific time.
   *
   * @param uriInfo             The URI information.
   * @param portalContainerName The portal container name.
   * @param identityId          The identity Id.
   *                            There is one special *identityId* called "me" standing for the authenticated user who makes this request.
   * @param format              The format of the returned result, for example, JSON or XML.
   * @param limit               The number of activities retrieved with the default value of 100. This input value must
   *                            be less than or equal to its default value (100). The number of the returned results is
   *                            actually less than or equal to the *limit* value.
   *                            If it is not specified, the default value will be 100.
   * @param sinceTime           Returns the activities having the created timestamps greater than the specified
   *                            *since\_time* timestamp.
   * @param maxTime             Returns the activities having the created timestamps less than the specified *max\_time*'s
   *                            created timestamp. Note that *since\_time* and *max\_time* must not be defined in one
   *                            request. If they are defined, the *since\_time* query param is chosen.
   * @param numberOfComments    Specifies the number of latest comments to be displayed along with each activity.
   *                            By default, *number\_of\_comments=0*. If *number\_of\_comments* is a positive number,
   *                            this number is considered as a limit number that must be equal or less than 100. If the
   *                            total number of comments is less than the provided positive number, the number of actual
   *                            comments must be returned. If the total number of comments is more than 100, it is
   *                            recommended to use *activity/\:activityId/comments.format* instead.
   * @param numberOfLikes       Specifies the number of the latest detailed likes to be returned along with this activity.
   *                            By default, *number\_of\_likes=0*. If *number\_of\_likes* is a positive number, this
   *                            number is considered as a limit number that must be equal or less than 100. If the total
   *                            number of likes is less than the provided positive number, the number of actual likes
   *                            must be returned. If the total number of likes is more than 100, it is recommended to
   *                            use *activity/\:activityId/likes.format* instead.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/socialdemo/activity_stream/f92cd6f0c0a80137102696ac26430766.json?limit=30&since_id=12345&number_of_likes=5
   * @response
   * {
   * "activities":[
   * {
   *       "id":"1a2b3c4d5e6f7g8h9j",
   *       "title":"Hello World!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 17 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId": "",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   * },
   * {
   *       "id":"1a210983123f7g8h9j",
   *       "title":"Hello World 1!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 19 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     }
   *   ]
   * }
   * @return the response
   * @LevelAPI Platform
   * @anchor ActivityStreamResources.getActivityStreamOfIdentityByTimestamp
   *
   */
  @GET
  @Path("{identityId}ByTimestamp.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActivityStreamOfIdentityByTimestamp(@Context UriInfo uriInfo,
                                                @PathParam("portalContainerName") String portalContainerName,
                                                @PathParam("identityId") String identityId,
                                                @PathParam("format") String format,
                                                @QueryParam("limit") int limit,
                                                @QueryParam("since_time") Long sinceTime,
                                                @QueryParam("max_time") Long maxTime,
                                                @QueryParam("number_of_comments") int numberOfComments,
                                                @QueryParam("number_of_likes") int numberOfLikes) {
    checkAuthenticatedRequest();
    PortalContainer portalContainer = checkValidPortalContainerName(portalContainerName);
    MediaType mediaType = checkSupportedFormat(format, SUPPORTED_FORMATS);
    Identity targetIdentity;
    if ("me".equals(identityId)) {
      targetIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    } else {
      targetIdentity = Util.getIdentityManager(portalContainerName).getIdentity(identityId, false);
    }

    if (targetIdentity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    //check permission
    boolean canAccess = SecurityManager.canAccessActivityStream(portalContainer,
                                                                Util.getAuthenticatedUserIdentity(portalContainerName),
                                                                targetIdentity);
    if (!canAccess) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    int maxLimit = limit <= 0 ? MAX_LIMIT : Math.min(limit, MAX_LIMIT);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);

    RealtimeListAccess<ExoSocialActivity> listAccess = null;
    List<ExoSocialActivity> activities = null;
    
    try {
      listAccess = activityManager.getActivitiesWithListAccess(targetIdentity);
      if (sinceTime != null) {
        activities = listAccess.loadNewer(sinceTime, maxLimit);
      } else if (maxTime != null) {
        activities = listAccess.loadOlder(maxTime, maxLimit);
      } else {
        activities = listAccess.loadAsList(0, maxLimit);
      }
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }

    ActivityRestListOut arlo = new ActivityRestListOut(activities, numberOfComments,
                                                       numberOfLikes, portalContainerName);

    return Util.getResponse(arlo, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Gets the activity stream feed of the authenticated user identity based on a specific time.
   *
   * @param uriInfo             The URI information.
   * @param portalContainerName The portal container name.
   * @param format              The format of the returned result, for example, JSON or XML.
   * @param limit               Specifies the number of activities to retrieve. It must be less than or equal to 100.
   *                            The value you pass as limit is a maximum number of activities to be returned.
   *                            The actual number of activities you receive maybe less than limit.
   *                            If it is not specified, the default value will be 100.
   * @param sinceTime           Returns the activities having the created timestamps greater than
   *                            the specified *since\_time* timestamp
   * @param maxTime             Returns the activities having the created timestamp less than the specified *max|_time*
   *                            timestamp. Note that *since\_time* and *max\_time* must not be defined in one request.
   *                            If they are defined, the *since\_time* query param is chosen.
   * @param numberOfComments    Specifies the latest number of comments to be displayed along with each activity.
   *                            By default, *number\_of\_comments=0*. If *number\_of\_comments* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number of
   *                            comments is less than the provided positive number, the number of actual comments must be
   *                            returned. If the total number of comments is more than 100,
   *                            it is recommended to use "*activity/\:activityId/comments.format*" instead
   * @param numberOfLikes       Specifies the latest number of detailed likes to be returned along with this activity.
   *                            By default, *number\_of\_likes=0*. If *number\_of\_likes* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number
   *                            of likes is less than the provided positive number, the number of actual likes must be
   *                            returned. If the total number of likes is more than 100, it is recommended to use
   *                            "*activity/\:activityId/likes.format*" instead.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/socialdemo/activity_stream/feedByTimestamp.json?limit=30&sinceTime=12345&number_of_comments=5&number_of_likes=5
   * @response
   * {
   *   "activities":[
   *     {
   *       "id":"1a2b3c4d5e6f7g8h9j",
   *       "title":"Hello World!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 17 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     },
   *     {
   *       "id":"1a210983123f7g8h9j",
   *       "title":"Hello World 1!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 19 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     }
   *   ]
   * }
   * @return the response
   * @LevelAPI Platform
   * @anchor ActivityStreamResources.getActivityFeedOfAuthenticatedByTimestamp
   *
   */
  @GET
  @Path("feedByTimestamp.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActivityFeedOfAuthenticatedByTimestamp(@Context UriInfo uriInfo,
                                                 @PathParam("portalContainerName") String portalContainerName,
                                                 @PathParam("format") String format,
                                                 @QueryParam("limit") int limit,
                                                 @QueryParam("since_time") Long sinceTime,
                                                 @QueryParam("max_time") Long maxTime,
                                                 @QueryParam("number_of_comments") int numberOfComments,
                                                 @QueryParam("number_of_likes") int numberOfLikes) {
    checkAuthenticatedRequest();
    checkValidPortalContainerName(portalContainerName);
    MediaType mediaType = checkSupportedFormat(format, SUPPORTED_FORMATS);

    Identity sourceIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);

    int maxLimit = limit <= 0 ? MAX_LIMIT : Math.min(limit, MAX_LIMIT);
    
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);

    RealtimeListAccess<ExoSocialActivity> listAccess = null;
    List<ExoSocialActivity> activities = null;
    
    try {
      listAccess = activityManager.getActivityFeedWithListAccess(sourceIdentity);
      if (sinceTime != null) {
        activities = listAccess.loadNewer(sinceTime, maxLimit);
      } else if (maxTime != null) {
        activities = listAccess.loadOlder(maxTime, maxLimit);
      } else {
        activities = listAccess.loadAsList(0, maxLimit);
      }
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    ActivityRestListOut activityRestListOut = new ActivityRestListOut(activities, numberOfComments,
                                                       numberOfLikes, portalContainerName);
    return Util.getResponse(activityRestListOut, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Gets space activities based on a specific time.
   *
   * @param uriInfo             The URI information.
   * @param portalContainerName The portal container name.
   * @param format              The format of the returned result, for example, JSON or XML.
   * @param limit               Specifies the number of activities to retrieve. It must be less than or equal to 100.
   *                            The value you pass as limit is a maximum number of activities to be returned.
   *                            The actual number of activities you receive maybe less than limit.
   *                            If it is not specified, the default value will be 100.
   * @param sinceTime           Returns the activities having the created timestamps greater than
   *                            the specified *since\_time*'s created timestamp.
   * @param maxTime             Returns the activities having the created timestamp less than the specified *max\_time*'s created
   *                            timestamp. Note that *since\_time* and *max\_time* must not be defined in one request,
   *                            if they are defined, the *since\_time* query param is chosen.
   * @param numberOfComments    Specifies the latest number of comments to be displayed along with each activity.
   *                            By default, *number\_of\_comments=0*. If *number\_of\_comments* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number of
   *                            comments is less than the provided positive number, the number of actual comments must be
   *                            returned. If the total number of comments is more than 100,
   *                            it is recommended to use "*activity/\:activityId/comments.format*" instead.
   * @param numberOfLikes       Specifies the latest number of detailed likes to be returned along with this activity.
   *                            By default, *number\_of\_likes=0*. If *number\_of\_likes* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number
   *                            of likes is less than the provided positive number, the number of actual likes must be
   *                            returned. If the total number of likes is more than 100, it is recommended to use
   *                            "*activity/\:activityId/likes.format*" instead.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/socialdemo/activity_stream/spaces.json?limit=30&since_id=12345&number_of_comments=5&number_of_likes=5
   * @response
   * {
   *   "activities":[
   *     {
   *       "id":"1a2b3c4d5e6f7g8h9j",
   *       "title":"Hello World!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 17 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     },
   *     {
   *       "id":"1a210983123f7g8h9j",
   *       "title":"Hello World 1!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 19 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     }
   *   ]
   * }
   * @return the response
   * @LevelAPI Platform
   * @anchor ActivityStreamResources.getActivitySpacesOfAuthenticatedByTimestamp
   *
   */
  @GET
  @Path("spacesByTimestamp.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActivitySpacesOfAuthenticatedByTimestamp(@Context UriInfo uriInfo,
                                                   @PathParam("portalContainerName") String portalContainerName,
                                                   @PathParam("format") String format,
                                                   @QueryParam("limit") int limit,
                                                   @QueryParam("since_time") Long sinceTime,
                                                   @QueryParam("max_time") Long maxTime,
                                                   @QueryParam("number_of_comments") int numberOfComments,
                                                   @QueryParam("number_of_likes") int numberOfLikes) {
    checkAuthenticatedRequest();
    checkValidPortalContainerName(portalContainerName);
    MediaType mediaType = checkSupportedFormat(format, SUPPORTED_FORMATS);

    Identity targetIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);

    int maxLimit = limit == 0 ? MAX_LIMIT : limit;
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);

    RealtimeListAccess<ExoSocialActivity> listAccess = null;
    List<ExoSocialActivity> activities = null;
    
    try {
      listAccess = activityManager.getActivitiesOfUserSpacesWithListAccess(targetIdentity);
      if (sinceTime != null) {
        activities = listAccess.loadNewer(sinceTime, maxLimit);
      } else if (maxTime != null) {
        activities = listAccess.loadOlder(maxTime, maxLimit);
      } else {
        activities = listAccess.loadAsList(0, maxLimit);
      }
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
   
   ActivityRestListOut arlo = new ActivityRestListOut(activities, numberOfComments, numberOfLikes, portalContainerName);

   return Util.getResponse(arlo, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Gets activities of connections of a specified identity based on a specific time.
   *
   * @param uriInfo             The URI information.
   * @param portalContainerName The portal container name
   * @param format              The format of the returned result, for example, JSON or XML.
   * @param limit               Specifies the number of activities to retrieve. It must be less than or equal to 100.
   *                            The value you pass as limit is a maximum number of activities to be returned.
   *                            The actual number of activities you receive maybe less than limit.
   *                            If it is not specified, the default value will be 100.
   * @param sinceTime           Returns the activities having the created timestamps greater than
   *                            the specified *since\_time* timestamp.
   * @param maxTime             Returns the activities having the created timestamp less than the specified *max\_time*
   *                            timestamp. Note that *since\_time* and *max\_time* must not be defined in one request,
   *                            if they are defined, the *since_time* query param is chosen.
   * @param numberOfComments    Specifies the latest number of comments to be displayed along with each activity.
   *                            By default, *number\_of\_comments=0*. If *number\_of\_comments* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number of
   *                            comments is less than the provided positive number, the number of actual comments must be
   *                            returned. If the total number of comments is more than 100,
   *                            it is recommended to use "*activity/\:activityId/comments.format*" instead.
   * @param numberOfLikes       Specifies the latest number of detailed likes to be returned along with this activity.
   *                            By default, *number\_of\_likes=0*. If *number\_of\_comments* is a positive number, this number is
   *                            considered as a limit number that must be equal or less than 100. If the actual number
   *                            of likes is less than the provided positive number, the number of actual likes must be
   *                            returned. If the total number of likes is more than 100, it is recommended to use
   *                            "*activity/\:activityId/likes.format*" instead.
   * @authentication
   * @request
   * GET: http://localhost:8080/rest/private/api/social/v1-alpha3/socialdemo/activity_stream/connections.json?limit=30&sinceTime=12345&number_of_comments=5&number_of_likes=5
   * @response
   * {
   *   "activities":[
   *     {
   *       "id":"1a2b3c4d5e6f7g8h9j",
   *       "title":"Hello World!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 17 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     },
   *     {
   *       "id":"1a210983123f7g8h9j",
   *       "title":"Hello World 1!!!",
   *       "appId":"",
   *       "type":"DEFAULT_ACTIVITY",
   *       "postedTime":123456789,
   *       "createdAt":"Fri Jun 19 06:42:26 +0000 2011",
   *       "priority":0.5,
   *       "templateParams":{
   *
   *       },
   *       "titleId":"",
   *       "body": "",
   *       "identityId":"123456789abcdefghi",
   *       "liked":true,
   *       "likedByIdentities":[
   *         {
   *           "id":"123456313efghi",
   *           "providerId":"organization",
   *           "remoteId":"demo",
   *           "profile":{
   *             "fullName":"Demo GTN",
   *             "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *           }
   *         }
   *       ],
   *       "totalNumberOfLikes":20,
   *       "posterIdentity":{
   *         "id":"123456313efghi",
   *         "providerId":"organization",
   *         "remoteId":"demo",
   *         "profile":{
   *           "fullName":"Demo GTN",
   *           "avatarUrl":"http://localhost:8080/profile/u/demo/avatar.jpg?u=12345"
   *         }
   *       },
   *       "comments":[
   *         {
   *
   *         }
   *       ],
   *       "totalNumberOfComments":1234,
   *       "activityStream":{
   *         "type":"user",
   *         "prettyId":"root",
   *         "fullName": "Root Root",
   *         "faviconUrl":"http://demo3.exoplatform.org/favicons/exo-default.jpg",
   *         "title":"Activity Stream of Root Root",
   *         "permaLink":"http://localhost:8080/profile/root"
   *       }
   *     }
   *   ]
   * }
   * @return the response
   * @LevelAPI Platform
   * @anchor ActivityStreamResource.getActivityConnectionsOfAuthenticatedByTimestamp
   *
   */
  @GET
  @Path("connectionsByTimestamp.{format}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getActivityConnectionsOfAuthenticatedByTimestamp(@Context UriInfo uriInfo,
                                                        @PathParam("portalContainerName") String portalContainerName,
                                                        @PathParam("format") String format,
                                                        @QueryParam("limit") int limit,
                                                        @QueryParam("since_time") Long sinceTime,
                                                        @QueryParam("max_time") Long maxTime,
                                                        @QueryParam("number_of_comments") int numberOfComments,
                                                        @QueryParam("number_of_likes") int numberOfLikes) {

    RestChecker.checkAuthenticatedRequest();
    RestChecker.checkValidPortalContainerName(portalContainerName);
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    MediaType mediaType = RestChecker.checkSupportedFormat(format, SUPPORTED_FORMATS);

    Identity targetIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);

    int maxLimit = limit <= 0 ? MAX_LIMIT : Math.min(limit, MAX_LIMIT);

    RealtimeListAccess<ExoSocialActivity> listAccess = null;
    List<ExoSocialActivity> activities = null;
    
    try {
      listAccess = activityManager.getActivitiesOfConnectionsWithListAccess(targetIdentity);
      if (sinceTime != null) {
        activities = listAccess.loadNewer(sinceTime, maxLimit);
      } else if (maxTime != null) {
        activities = listAccess.loadOlder(maxTime, maxLimit);
      } else {
        activities = listAccess.loadAsList(0, maxLimit);
      }
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }

    ActivityRestListOut activityRestListOut = new ActivityRestListOut(activities, numberOfComments,
            numberOfLikes, portalContainerName);

    return Util.getResponse(activityRestListOut, uriInfo, mediaType, Response.Status.OK);
  }
}
