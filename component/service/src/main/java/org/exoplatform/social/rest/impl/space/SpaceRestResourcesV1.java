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
package org.exoplatform.social.rest.impl.space;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestProperties;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.api.SpaceRestResources;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.BaseEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.rest.api.models.ActivityRestIn;

@Path(VersionResources.VERSION_ONE + "/social/spaces")
@Api(value=VersionResources.VERSION_ONE + "/social/spaces", description = "Operations eXo social space.")
public class SpaceRestResourcesV1 implements SpaceRestResources {

  public SpaceRestResourcesV1() {
  }
  
  /**
   * {@inheritDoc}
   */
  @RolesAllowed("users")
  @ApiOperation(value = "Get spaces",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request space found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find spaces.") })
  public Response getSpaces(@Context UriInfo uriInfo,
                            @ApiParam(value = "Space name search condition.", required = false) @QueryParam("q") String q,
                            @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                            @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                            @ApiParam(value = "Size of returned result list.", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                            @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {

    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    
    ListAccess<Space> listAccess = null;
    SpaceFilter spaceFilter = null;
    if (q != null) {
      spaceFilter = new SpaceFilter();
      spaceFilter.setSpaceNameSearchCondition(q);
    }
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    if (RestUtils.isMemberOfAdminGroup()) {
      listAccess = spaceService.getAllSpacesByFilter(spaceFilter);
    } else {
      listAccess = spaceService.getAccessibleSpacesByFilter(authenticatedUser, spaceFilter);
    }
    List<DataEntity> spaceInfos = new ArrayList<DataEntity>();
    for (Space space : listAccess.load(offset, limit)) {
      SpaceEntity spaceInfo = EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand);
      //
      spaceInfos.add(spaceInfo.getDataEntity()); 
    }
    CollectionEntity collectionSpace = new CollectionEntity(spaceInfos, EntityBuilder.SPACES_TYPE, offset, limit);
    if (returnSize) {
      collectionSpace.setSize( listAccess.getSize());
    }
    
    return EntityBuilder.getResponse(collectionSpace, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Create space",
                httpMethod = "POST",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Space created successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to create space.") })
  public Response createSpace(@Context UriInfo uriInfo,
                              @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand,
                              @ApiParam(value = "Created space object. Space object named \"my space\" as below:<br />" +
                              		              "{<br />\"displayName\": \"my space\"," +
                                                "<br />\"description\": \"This is my space\"," +
                                                "<br />\"groupId\": \"/spaces/my_space\"," +
                                                "<br />\"visibility\": \"private\"," +
                                                "<br />\"subscription\": \"validation\"<br />}" 
                                                , required = true) SpaceEntity model) throws Exception {
    if (model == null || model.getDisplayName() == null || model.getDisplayName().length() == 0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }

    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    // validate the display name
    if (spaceService.getSpaceByDisplayName(model.getDisplayName()) != null) {
      throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    Space space = new Space();
    fillSpaceFromModel(space, model);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + space.getPrettyName());
    space.setType(DefaultSpaceApplicationHandler.NAME);
    String[] managers = new String[] {authenticatedUser};
    String[] members = new String[] {authenticatedUser};
    space.setManagers(managers);
    space.setMembers(members);
    //
    spaceService.createSpace(space, authenticatedUser);

    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @ApiOperation(value = "Get space by id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request space found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find space by Id") })
  public Response getSpaceById(@Context UriInfo uriInfo,
                               @ApiParam(value = "space id", required = true) @PathParam("id") String id,
                               @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (Space.HIDDEN.equals(space.getVisibility()) && ! spaceService.isMember(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Update space by Id",
                httpMethod = "PUT",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Space updated successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find space.") })
  public Response updateSpaceById(@Context UriInfo uriInfo,
                                  @ApiParam(value = "space id", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand,
                                  @ApiParam(value = "Space entity for updating", required = true) SpaceEntity model) throws Exception {
    
    if (model == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isManager(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    fillSpaceFromModel(space, model);
    spaceService.updateSpace(space);
    
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")

  @ApiOperation(value = "Delete space by Id",
                httpMethod = "DELETE",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Space deleted successfully"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to delete space.") })
  public Response deleteSpaceById(@Context UriInfo uriInfo,
                                  @ApiParam(value = "space id", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isManager(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    spaceService.deleteSpace(space);
    
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromSpace(space, authenticatedUser, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}/users")
  @RolesAllowed("users")
  @ApiOperation(value = "Get members of a space",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request space members found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find space members.") })
  public Response getSpaceMembers(@Context UriInfo uriInfo,
                                  @ApiParam(value = "space id", required = true) @PathParam("id") String id,
                                  @ApiParam(value = "Role of user in space. ex: manager", required = false, defaultValue = "0") @QueryParam("role") String role,
                                  @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                                  @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                                  @ApiParam(value = "Size of returned result list.", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
                                  @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! RestUtils.isMemberOfAdminGroup())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    String[] users = (role != null && role.equals("manager")) ? space.getManagers() : space.getMembers();
    int size = users.length;
    //
    if (limit > 0) {
      users = Arrays.copyOfRange(users, offset > size - 1 ? size - 1 : offset, (offset + limit > size) ? size : (offset + limit));
    }
    List<DataEntity> profileInfos = EntityBuilder.buildEntityProfiles(users, uriInfo.getPath(), expand);
    CollectionEntity collectionUser = new CollectionEntity(profileInfos, EntityBuilder.USERS_TYPE, offset, limit);
    if (returnSize) {
      collectionUser.setSize(size);
    }    
    return EntityBuilder.getResponse(collectionUser, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @GET
  @Path("{id}/activities")
  @RolesAllowed("users")
  @ApiOperation(value = "Get space activities by Id",
                httpMethod = "GET",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Given request space activities found"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to find get space activities.") })
  public Response getSpaceActivitiesById(@Context UriInfo uriInfo,
      @ApiParam(value = "space id", required = true) @PathParam("id") String id,
      @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
      @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
      @ApiParam(value = "Older base time to load activity (yyyy-MM-dd HH:mm:ss)", required = false) @QueryParam("before") String before,
      @ApiParam(value = "Newer base time to load activity (yyyy-MM-dd HH:mm:ss)", required = false) @QueryParam("after") String after,
      @ApiParam(value = "Size of returned result list.", defaultValue = "false") @QueryParam("returnSize") boolean returnSize,
      @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand) throws Exception {
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || ! spaceService.isMember(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity spaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    RealtimeListAccess<ExoSocialActivity> listAccess = CommonsUtils.getService(ActivityManager.class).getActivitiesOfSpaceWithListAccess(spaceIdentity);
    List<ExoSocialActivity> activities = null;
    if (after != null && RestUtils.getBaseTime(after) > 0) {
      activities = listAccess.loadNewer(RestUtils.getBaseTime(after), limit);
    } else if (before != null && RestUtils.getBaseTime(before) > 0) {
      activities = listAccess.loadOlder(RestUtils.getBaseTime(before), limit);
    } else {
      activities = listAccess.loadAsList(offset, limit);
    }
    List<DataEntity> activityEntities = new ArrayList<DataEntity>();
    //
    BaseEntity as = new BaseEntity(spaceIdentity.getRemoteId());
    as.setProperty(RestProperties.TYPE, EntityBuilder.SPACE_ACTIVITY_TYPE);
    //
    for (ExoSocialActivity activity : activities) {
      ActivityEntity activityInfo = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand);
      activityInfo.setActivityStream(as.getDataEntity());
      //
      activityEntities.add(activityInfo.getDataEntity());
    }
    CollectionEntity collectionActivity = new CollectionEntity(activityEntities, EntityBuilder.ACTIVITIES_TYPE,  offset, limit);
    if(returnSize) {
      collectionActivity.setSize(listAccess.getSize());
    }
    return EntityBuilder.getResponse(collectionActivity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  /**
   * {@inheritDoc}
   */
  @POST
  @Path("{id}/activities")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Post activity to space",
                httpMethod = "POST",
                response = Response.class,
                notes = "This can only be done by the logged in user.")
  @ApiResponses(value = { 
    @ApiResponse (code = 200, message = "Activity posted successfully on space"),
    @ApiResponse (code = 500, message = "Internal server error"),
    @ApiResponse (code = 400, message = "Invalid query input to post activity on space.") })
  public Response postActivityOnSpace(@Context UriInfo uriInfo,
                                      @ApiParam(value = "space id", required = true) @PathParam("id") String id,
                                      @ApiParam(value = "Expand param : ask for a full representation of a subresource", required = false) @QueryParam("expand") String expand,
                                      @ApiParam(value = "Created activity object", required = true) ActivityRestIn model) throws Exception {
    if (model == null || model.getTitle() == null || model.getTitle().length() == 0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    //
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceById(id);
    if (space == null || ! spaceService.isMember(space, authenticatedUser)) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    Identity spaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    Identity poster = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, false);
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(model.getTitle());
    activity.setUserId(poster.getId());
    CommonsUtils.getService(ActivityManager.class).saveActivityNoReturn(spaceIdentity, activity);
    
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), expand), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }

  private void fillSpaceFromModel(Space space, SpaceEntity model) {
    space.setDisplayName(model.getDisplayName());
    if (model.getDescription() != null && model.getDescription().length() > 0) {
      space.setDescription(StringEscapeUtils.escapeHtml(model.getDescription()));
    }
    if (space.getGroupId() == null) {
      String groupId = model.getDisplayName();
      if (model.getGroupId() != null && model.getGroupId().length() > 0) {
        groupId = model.getGroupId();
        if (groupId.indexOf("/") >= 0) {
          groupId = groupId.substring(groupId.lastIndexOf("/") + 1);
        }
        if (groupId == "") {
          groupId = model.getDisplayName();
        }
      }
      space.setPrettyName(groupId);
    }
    
    if (model.getVisibility() != null && (model.getVisibility().equals(Space.HIDDEN) 
        || model.getVisibility().equals(Space.PRIVATE))) {
      space.setVisibility(model.getVisibility());
    } else if (space.getVisibility() == null || space.getVisibility().length() == 0) {
      space.setVisibility(Space.PRIVATE);
    }
    
    if (Space.OPEN.equals(model.getSubscription()) || Space.CLOSE.equals(model.getSubscription())) {
      space.setRegistration(model.getSubscription());
    } else if (space.getRegistration() == null || space.getRegistration().length() == 0) {
      space.setRegistration(Space.VALIDATION);
    }
  }
}
