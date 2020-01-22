/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.social.rest.impl.user;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.user.UserStateModel;
import org.exoplatform.services.user.UserStateService;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ActivityFile;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.ErrorResource;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.api.UserRestResources;
import org.exoplatform.social.rest.entity.*;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.ActivityFileRestIn;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * Provides REST Services for manipulating jobs related to users.
 * 
 */

@Path(VersionResources.VERSION_ONE + "/social/users")
@Api(tags = VersionResources.VERSION_ONE + "/social/users", value = VersionResources.VERSION_ONE + "/social/users", description = "Operations on users with their activities, connections and spaces")
public class UserRestResourcesV1 implements UserRestResources {

  private static final String ONLINE = "online";
  private UserACL userACL;

  private IdentityManager identityManager;

  private UserStateService userStateService;

  private SpaceService spaceService;

  public static enum ACTIVITY_STREAM_TYPE {
    all, owner, connections, spaces
  }

  private static final String INVISIBLE = "invisible";

  private static final Log LOG = ExoLogger.getLogger(UserRestResourcesV1.class);
  
  public UserRestResourcesV1(UserACL userACL, IdentityManager identityManager, UserStateService userStateService, SpaceService spaceService) {
    this.userACL = userACL;
    this.identityManager = identityManager;
    this.userStateService = userStateService;
    this.spaceService = spaceService;
  }
  
  @GET
  @RolesAllowed("users")
  @ApiOperation(value = "Gets all users",
                httpMethod = "GET",
                response = Response.class,
                notes = "Using the query param \"q\" to filter the target users, ex: \"q=jo*\" returns all the users beginning by \"jo\"."
                + "Using the query param \"status\" to filter the target users, ex: \"status=online*\" returns the visible online users."
                + "Using the query params \"status\" and \"spaceId\" together to filter the target users, ex: \"status=online*\" and \"spaceId=1*\" returns the visible online users who are member of space with id=1."
                + "The params \"status\" and \"spaceId\" cannot be used with \"q\" param since it will falsify the \"limit\" param which is 20 by default. If these 3 parameters are used together, the parameter \"q\" will be ignored")
  @ApiResponses(value = {
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 404, message = "Resource not found"),
    @ApiResponse (code = 500, message = "Internal server error due to data encoding"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getUsers(@Context UriInfo uriInfo,
                           @ApiParam(value = "User name information to filter, ex: user name, last name, first name or full name", required = false) @QueryParam("q") String q,
                           @ApiParam(value = "User status to filter online users, ex: online", required = false) @QueryParam("status") String status,
                           @ApiParam(value = "Space id to filter only its members, ex: 1", required = false) @QueryParam("spaceId") String spaceId,
                           @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                           @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                           @ApiParam(value = "Returning the number of users found or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                           @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {

    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);

    Identity[] identities;
    int totalSize = 0;

    if (StringUtils.isNotBlank(status) && ONLINE.equals(status)) {
      String userId;
      try {
        userId = ConversationState.getCurrent().getIdentity().getUserId();
      } catch (Exception e) {
        return Response.status(HTTPStatus.UNAUTHORIZED).build();
      }
      if (StringUtils.isBlank(userId)) {
        return Response.status(HTTPStatus.UNAUTHORIZED).build();
      }
      Space space = null;
      if (StringUtils.isNotBlank(spaceId)) {
        space = spaceService.getSpaceById(spaceId);
        if (space != null) {
          identities = getOnlineIdentitiesOfSpace(userId, space, limit);
        } else {
          return EntityBuilder.getResponse(new ErrorResource("space " + spaceId + " does not exist", "space not found"), uriInfo, RestUtils.getJsonMediaType(), Response.Status.NOT_FOUND);
        }
      } else {
        identities = getOnlineIdentities(userId, limit);
      }
    } else {
      ProfileFilter filter = new ProfileFilter();
      filter.setName(q == null || q.isEmpty() ? "" : q);
      ListAccess<Identity> list = CommonsUtils.getService(IdentityManager.class).getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, false);
      identities = list.load(offset, limit);
      if(returnSize) {
        totalSize = list.getSize();
      }
    }
    List<DataEntity> profileInfos = new ArrayList<DataEntity>();
    for (Identity identity : identities) {
      ProfileEntity profileInfo = EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand);
      //
      profileInfos.add(profileInfo.getDataEntity());
    }
    CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
    if (returnSize) {
      collectionUser.setSize(totalSize);
    }

    return EntityBuilder.getResponse(collectionUser, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Creates a new user",
                httpMethod = "POST",
                response = Response.class,
                notes = "This creates the user if the authenticated user is in the /platform/administrators group.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response addUser(@Context UriInfo uriInfo,
                          @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                          @ApiParam(value = "User object to be created, ex:<br />" +
                                            "{<br />\"username\": \"john\"," +
                                            "<br />\"password\": \"gtngtn\"," +
                                            "<br />\"email\": \"john@exoplatform.com\"," +
                                            "<br />\"firstname\": \"John\"," +
                                            "<br />\"lastname\": \"Smith\"<br />}"
                          		              , required = true) UserEntity model) throws Exception {
    if (model.isNotValid()) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    //Check permission of current user
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    //check if the user is already exist
    Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, model.getUsername(), true);
    if (identity != null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    if(getUserByEmail(model.getEmail()) != null) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    //Create new user
    UserHandler userHandler = CommonsUtils.getService(OrganizationService.class).getUserHandler();
    User user = userHandler.createUserInstance(model.getUsername());
    user.setFirstName(model.getFirstname());
    user.setLastName(model.getLastname());
    user.setEmail(model.getEmail());
    user.setPassword(model.getPassword() == null || model.getPassword().isEmpty() ? "exo" : model.getPassword());
    userHandler.createUser(user, true);
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(model.getUsername(), uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets a specific user by user name",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Request fulfilled"),
    @ApiResponse (code = 404, message = "Resource not found"),
    @ApiResponse (code = 500, message = "Internal server error due to data encoding"),
    @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getUserById(@Context UriInfo uriInfo,
                              @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                              @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    //
    if (identity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   *
   * @param uriInfo
   * @param id
   * @return
   * @throws IOException
   */
  @GET
  @Path("{id}/avatar")
  @ApiOperation(value = "Gets a specific user avatar by username",
          httpMethod = "GET",
          response = Response.class,
          notes = "This can only be done by the logged in user.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 404, message = "Resource not found"),
          @ApiResponse (code = 500, message = "Internal server error due to data encoding"),
          @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getUserAvatarById(@Context UriInfo uriInfo,
                                    @Context Request request,
                                    @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                    @ApiParam(value = "URL to default avatar Or '404' to return a 404 http code", required = false) @QueryParam("default") String defaultAvatar) throws IOException {
  
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);

    //
    Response.ResponseBuilder builder = null;
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else {
      //
      Profile profile = identity.getProfile();
      Long lastUpdated = null;
      if (profile != null) {
        lastUpdated = profile.getAvatarLastUpdated();
      }
      EntityTag eTag = null;
      if (lastUpdated != null) {
        eTag = new EntityTag(Integer.toString(lastUpdated.hashCode()));
      }
      //
      if (identity.isEnable() && !identity.isDeleted()) {
        builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
        if (builder == null) {
          InputStream stream = identityManager.getAvatarInputStream(identity);
          if (stream != null) {
          /* As recommended in the the RFC1341 (https://www.w3.org/Protocols/rfc1341/4_Content-Type.html),
          we set the avatar content-type to "image/png". So, its data  would be recognized as "image" by the user-agent */
            builder = Response.ok(stream, "image/png");
            builder.tag(eTag);
          }
        }
      }

      if (builder == null) {
        builder = getDefaultAvatarBuilder(defaultAvatar);
      }

      CacheControl cc = new CacheControl();
      cc.setMaxAge(86400);
      builder.cacheControl(cc);
      return builder.cacheControl(cc).build();
    }
  }

  /**
   *
   * @param uriInfo
   * @param id
   * @return
   * @throws IOException
   */
  @GET
  @Path("{id}/banner")
  @ApiOperation(value = "Gets a specific user banner by username",
          httpMethod = "GET",
          response = Response.class,
          notes = "This can only be done by the logged in user.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "Request fulfilled"),
          @ApiResponse (code = 404, message = "Resource not found"),
          @ApiResponse (code = 500, message = "Internal server error due to data encoding"),
          @ApiResponse (code = 400, message = "Invalid query input") })
  public Response getUserBannerById(@Context UriInfo uriInfo,
                                    @Context Request request,
                                    @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                    @ApiParam(value = "URL to default banner Or '404' to return a 404 http code", required = false) @QueryParam("default") String defaultBanner) throws IOException {

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);

    //
    Response.ResponseBuilder builder = null;
    if (identity == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else {
      //
      Profile profile = identity.getProfile();
      Long lastUpdated = null;
      if (profile != null) {
        lastUpdated = profile.getBannerLastUpdated();
      }
      EntityTag eTag = null;
      if (lastUpdated != null) {
        eTag = new EntityTag(Integer.toString(lastUpdated.hashCode()));
      }
      //
      builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
      if (builder == null) {
        InputStream stream = identityManager.getBannerInputStream(identity);
        if (stream != null) {
          /* As recommended in the the RFC1341 (https://www.w3.org/Protocols/rfc1341/4_Content-Type.html),
          we set the banner content-type to "image/png". So, its data  would be recognized as "image" by the user-agent */
          builder = Response.ok(stream, "image/png");
          builder.tag(eTag);
        }
      }

      if (builder == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      CacheControl cc = new CacheControl();
      cc.setMaxAge(86400);
      builder.cacheControl(cc);
      return builder.cacheControl(cc).build();
    }
  }

  private Response.ResponseBuilder getDefaultAvatarBuilder(String avatarUrl) {
      if (avatarUrl != null) {
        if (avatarUrl.equals("404")) {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        try {
          URL url = new URL(avatarUrl);
          String type = url.openConnection().getHeaderField("Content-Type");
          if (type != null && type.startsWith("image/")) {
            InputStream input = url.openStream();
            return Response.ok(input, type);
          }
        } catch (IOException e) {
          LOG.debug("Could NOT open the default url " + avatarUrl);
        }
      }

    InputStream is = PortalContainer.getInstance().getPortalContext().getResourceAsStream("/skin/images/system/UserAvtDefault.png");
    if (is == null) {
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
    return Response.ok(is, "image/png");
  }

  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Deletes a specific user by user name",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This deletes the user if the authenticated user is in the /platform/administrators group.")
  public Response deleteUserById(@Context UriInfo uriInfo,
                                 @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                 @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    //Check permission of current user
    if (!RestUtils.isMemberOfAdminGroup()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (identity == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    identityManager.hardDeleteIdentity(identity);
    identity.setDeleted(true);
    // Deletes the user on Portal side
    UserHandler userHandler = CommonsUtils.getService(OrganizationService.class).getUserHandler();
    userHandler.removeUser(id, false);
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Updates a specific user by user name",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This updates the user if he is the authenticated user.")
  public Response updateUserById(@Context UriInfo uriInfo,
                                 @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                 @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand,
                                 @ApiParam(value = "User object to be updated, ex:<br />" +
                                            "{<br />\"username\": \"john\"," +
                                            "<br />\"password\": \"gtngtn\"," +
                                            "<br />\"email\": \"john@exoplatform.com\"," +
                                            "<br />\"firstname\": \"John\"," +
                                            "<br />\"lastname\": \"Smith\"<br />}", required = true) UserEntity model) throws Exception {
    UserHandler userHandler = CommonsUtils.getService(OrganizationService.class).getUserHandler();
    User user = userHandler.findUserByName(id);
    if (user == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    //Check if the current user is the authenticated user
    if (!ConversationState.getCurrent().getIdentity().getUserId().equals(id)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    if(getUserByEmail(model.getEmail()) != null && 
        !user.getUserName().equals(getUserByEmail(model.getEmail()).getUserName())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    fillUserFromModel(user, model);
    userHandler.saveUser(user, true);
    //
    return EntityBuilder.getResponse(EntityBuilder.buildEntityProfile(id, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/connections")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets connections of a specific user",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  public Response getConnectionOfUser(@Context UriInfo uriInfo,
                                      @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                      @ApiParam(value = "Returning the number of connections or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                      @ApiParam(value = "Asking for a full representation of a specific subresource if any", required = false) @QueryParam("expand") String expand) throws Exception {
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    
    int limit = RestUtils.getLimit(uriInfo);
    int offset = RestUtils.getOffset(uriInfo);
    
    List<DataEntity> profileInfos = new ArrayList<DataEntity>();
    ListAccess<Identity> listAccess = CommonsUtils.getService(RelationshipManager.class).getConnectionsByFilter(target, new ProfileFilter());
    Identity []identities = listAccess.load(offset, limit);
    for (Identity identity : identities) {
      ProfileEntity profileInfo = EntityBuilder.buildEntityProfile(identity.getProfile(), uriInfo.getPath(), expand);
      //
      profileInfos.add(profileInfo.getDataEntity());
    }
    CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
    if(returnSize) {
      collectionUser.setSize(listAccess.getSize());
    }
    return EntityBuilder.getResponse(collectionUser, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/spaces")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets spaces of a specific user",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns a list of spaces in the following cases: <br/><ul><li>the given user is the authenticated user</li><li>the authenticated user is in the group /platform/administrators</li></ul>")
  public Response getSpacesOfUser(@Context UriInfo uriInfo,
                                  @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                  @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                  @ApiParam(value = "Returning the number of spaces or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                  @ApiParam(value = "Asking for a full representation of a specific subresource, ex: <em>members</em> or <em>managers</em>", required = false) @QueryParam("expand") String expand) throws Exception {
    
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
    
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    //Check if the given user exists
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    //Check permission of authenticated user : he must be an admin or he is the given user
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (!userACL.getSuperUser().equals(authenticatedUser) && !authenticatedUser.equals(id) ) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    
    List<DataEntity> spaceInfos = new ArrayList<DataEntity>();
    ListAccess<Space> listAccess = CommonsUtils.getService(SpaceService.class).getMemberSpaces(id);
    
    for (Space space : listAccess.load(offset, limit)) {
      SpaceEntity spaceInfo = EntityBuilder.buildEntityFromSpace(space, id, uriInfo.getPath(), expand);
      //
      spaceInfos.add(spaceInfo.getDataEntity()); 
    }
    CollectionEntity collectionSpace = new CollectionEntity(spaceInfos, EntityBuilder.SPACES_TYPE, offset, limit);
    if (returnSize) {
      collectionSpace.setSize( listAccess.getSize());
    }
    
    return EntityBuilder.getResponse(collectionSpace, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/activities")
  @RolesAllowed("users")
  @ApiOperation(value = "Gets activities of a specific user",
                httpMethod = "GET",
                response = Response.class,
                notes = "This returns an activity in the list in the following cases: <br/><ul><li>this is a user activity and the owner of the activity is the authenticated user or one of his connections</li><li>this is a space activity and the authenticated user is a member of the space</li></ul>")
  public Response getActivitiesOfUser(@Context UriInfo uriInfo,
                                      @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                      @ApiParam(value = "Activity stream type, ex: <em>owner, connections, spaces</em> or <em>all</em>", required = false, defaultValue = "all") @QueryParam("type") String type,
                                      @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                      @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                      @ApiParam(value = "Base time to load older activities (yyyy-MM-dd HH:mm:ss)", required = false) @QueryParam("before") String before,
                                      @ApiParam(value = "Base time to load newer activities (yyyy-MM-dd HH:mm:ss)", required = false) @QueryParam("after") String after,
                                      @ApiParam(value = "Returning the number of activities or not", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                      @ApiParam(value = "Asking for a full representation of a specific subresource, ex: <em>comments</em> or <em>likes</em>", required = false) @QueryParam("expand") String expand) throws Exception {
    
    offset = offset > 0 ? offset : RestUtils.getOffset(uriInfo);
    limit = limit > 0 ? limit : RestUtils.getLimit(uriInfo);
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //Check if the given user doesn't exist
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (target == null) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }
    ACTIVITY_STREAM_TYPE streamType;
    try {
      streamType = ACTIVITY_STREAM_TYPE.valueOf(type);
    } catch (Exception e) {
      streamType = ACTIVITY_STREAM_TYPE.all;
    }

    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    RealtimeListAccess<ExoSocialActivity> listAccess = null;
    List<ExoSocialActivity> activities = null;
    switch (streamType) {
      case all: {
        listAccess = activityManager.getActivityFeedWithListAccess(target);
        break;
      }
      case owner: {
        listAccess = activityManager.getActivitiesWithListAccess(target);
        break;
      }
      case connections: {
        listAccess = activityManager.getActivitiesOfConnectionsWithListAccess(target);
        break;
      }
      case spaces: {
        listAccess = activityManager.getActivitiesOfUserSpacesWithListAccess(target);
        break;
      }
      default:
        break;
    }
    //
    if (after != null && RestUtils.getBaseTime(after) > 0) {
      activities = listAccess.loadNewer(RestUtils.getBaseTime(after), limit);
    } else if (before != null && RestUtils.getBaseTime(before) > 0) {
      activities = listAccess.loadOlder(RestUtils.getBaseTime(before), limit);
    } else {
      activities = listAccess.loadAsList(offset, limit);
    }
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    List<DataEntity> activityEntities = new ArrayList<DataEntity>();
    for (ExoSocialActivity activity : activities) {
      DataEntity as = EntityBuilder.getActivityStream(activity, currentUser);
      if (as == null) continue;
      ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand);
      activityEntity.setActivityStream(as);
      //
      activityEntities.add(activityEntity.getDataEntity()); 
    }
    CollectionEntity collectionActivity = new CollectionEntity(activityEntities, EntityBuilder.ACTIVITIES_TYPE,  offset, limit);
    if(returnSize) {
      if (before != null || after != null) {
        collectionActivity.setSize(activities.size());
      } else {
        collectionActivity.setSize(listAccess.getSize());
      }
    }
    return EntityBuilder.getResponse(collectionActivity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @Path("{id}/activities")
  @RolesAllowed("users")
  @ApiOperation(value = "Creates an activity by a specific user",
                httpMethod = "POST",
                response = Response.class,
                notes = "This creates the activity if the given user is the authenticated user.")
  public Response addActivityByUser(@Context UriInfo uriInfo,
                                    @ApiParam(value = "User name", required = true) @PathParam("id") String id,
                                    @ApiParam(value = "Asking for a full representation of a specific subresource, ex: <em>comments</em> or <em>likes</em>", required = false) @QueryParam("expand") String expand,
                                    @ApiParam(value = "Activity object to be created, in which the title of activity is required, ex: <br/>{\"title\": \"act4 posted\"}", required = true) ActivityRestIn model) throws Exception {
    if (model == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //Check if the given user doesn't exist
    Identity target = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, id, true);
    if (target == null || !ConversationState.getCurrent().getIdentity().getUserId().equals(id)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(model.getTitle());
    activity.setBody(model.getBody());
    activity.setType(model.getType());
    activity.setUserId(target.getId());
    if(model.getFiles() != null) {
      activity.setFiles(model.getFiles()
              .stream()
              .map(fileModel -> new ActivityFile(fileModel.getUploadId(), fileModel.getStorage()))
              .collect(Collectors.toList()));
    }
    CommonsUtils.getService(ActivityManager.class).saveActivityNoReturn(target, activity);

    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  private void fillUserFromModel(User user, UserEntity model) {
    if (model.getFirstname() != null && !model.getFirstname().isEmpty()) {
      user.setFirstName(model.getFirstname());
    }
    if (model.getLastname() != null && !model.getLastname().isEmpty()) {
      user.setLastName(model.getLastname());
    }
    if (model.getEmail() != null && !model.getEmail().isEmpty()) {
      user.setEmail(model.getEmail());
    }
    if (model.getPassword() != null && !model.getPassword().isEmpty()) {
      user.setPassword(model.getPassword());
    }
  }

  /**
   * gets online identities who are members of a space.
   *
   * @param userId The current user.
   * @param space The space of which extract members.
   * @param limit Maximum number of identities to return.
   * @return identity array.
   */
  private Identity[] getOnlineIdentitiesOfSpace(String userId, Space space, int limit) {
    List<Identity> identities = new ArrayList<>();
    String[] spaceMembers = space.getMembers();
    String superUserName = userACL.getSuperUser();
    for (String user : spaceMembers) {
        UserStateModel userModel = userStateService.getUserState(user);
        boolean isOnline = userStateService.isOnline(user);
        if (user.equals(userId) || user.equals(superUserName) || userModel == null || INVISIBLE.equals(userModel.getStatus()) || !isOnline) {
          continue;
        }
        Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, false);
        identities.add(userIdentity);
        if (identities.size() == limit) {
          break;
        }
    }
    return identities.toArray(new Identity[identities.size()]);
  }

  /**
   * gets online identities.
   *
   * @param userId The current user.
   * @param limit Maximum number of identities to return.
   * @return identity array.
   */
  private Identity[] getOnlineIdentities(String userId, int limit) {
    List<Identity> identities = new ArrayList<>();
    List<UserStateModel> users = userStateService.online();
    Collections.reverse(users);
    if (users.size() > limit) {
      users = users.subList(0, limit);
    }
    String superUserName = userACL.getSuperUser();
    for (UserStateModel userModel : users) {
      String user = userModel.getUserId();
      if (user.equals(userId) || user.equals(superUserName) || userModel == null || INVISIBLE.equals(userModel.getStatus()))
        continue;
      Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, false);
      identities.add(userIdentity);
    }
    return identities.toArray(new Identity[identities.size()]);
  }
  
  /**
   * Checks if input email is existing already or not.
   * 
   * @param email Input email to check.
   * @return true if email is existing in system.
   */
  public static User getUserByEmail(String email) {
    if (email == null) return null;
    try {
      Query query = new Query();
      query.setEmail(email);
      OrganizationService service = CommonsUtils.getService(OrganizationService.class);
      User[] users = service.getUserHandler().findUsersByQuery(query).load(0, 10);
      return users[0];
    } catch (Exception e) {
      return null;
    }
  }
}
