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
package org.exoplatform.social.rest.impl.activity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.rest.api.ActivityRestResources;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.service.rest.api.VersionResources;

@Path(VersionResources.VERSION_ONE + "/social/activities")
public class ActivityRestResourcesV1 implements ActivityRestResources {
  
  @GET
  @RolesAllowed("users")
  public Response getActivitiesOfCurrentUser(@Context UriInfo uriInfo) throws Exception {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    int limit = RestUtils.getLimit(uriInfo);
    int offset = RestUtils.getOffset(uriInfo);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getAllActivitiesWithListAccess();
    List<ExoSocialActivity> activities = listAccess.loadAsList(offset, limit);
    
    List<DataEntity> activityEntities = new ArrayList<DataEntity>();
    for (ExoSocialActivity activity : activities) {
      DataEntity as = EntityBuilder.getActivityStream(activity, currentUser);
      if (as == null) continue;
      ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"));
      activityEntity.setActivityStream(as);
      //
      activityEntities.add(activityEntity.getDataEntity()); 
    }
    CollectionEntity collectionActivity = new CollectionEntity(activityEntities, EntityBuilder.ACTIVITIES_TYPE,  offset, limit);
    if(RestUtils.isReturnSize(uriInfo)) {
      collectionActivity.setSize(listAccess.getSize());
    }

    return EntityBuilder.getResponse(collectionActivity, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}")
  @RolesAllowed("users")
  public Response getActivityById(@Context UriInfo uriInfo) throws Exception {
    String id = RestUtils.getPathParam(uriInfo, "id");
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    DataEntity as = EntityBuilder.getActivityStream(activity.isComment() ? activityManager.getParentActivity(activity) : activity, currentUser);
    if (as == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"));
    if (!activity.isComment()) {
      activityEntity.setActivityStream(as);
    }

    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @PUT
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response updateActivityById(@Context UriInfo uriInfo,
                                      ActivityEntity model) throws Exception {
    if (model == null || model.getTitle() == null || model.getTitle().length() == 0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    String id = RestUtils.getPathParam(uriInfo, "id");
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null || ! activity.getPosterId().equals(currentUser.getId())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    //update activity's title
    activity.setTitle(model.getTitle());
    activityManager.updateActivity(activity);
    
    DataEntity as = EntityBuilder.getActivityStream(activity, currentUser);
    ActivityEntity activityInfo = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"));
    activityInfo.setActivityStream(as);
    
    return EntityBuilder.getResponse(activityInfo.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @DELETE
  @Path("{id}")
  @RolesAllowed("users")
  public Response deleteActivityById(@Context UriInfo uriInfo) throws Exception {
    String id = RestUtils.getPathParam(uriInfo, "id");
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null || ! activity.getPosterId().equals(currentUser.getId()) || ! activity.getStreamOwner().equals(currentUser.getRemoteId())) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    //
    DataEntity as = EntityBuilder.getActivityStream(activity, currentUser);
    ActivityEntity activityEntity = EntityBuilder.buildEntityFromActivity(activity, uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"));
    activityEntity.setActivityStream(as);
    //
    activityManager.deleteActivity(activity);
    return EntityBuilder.getResponse(activityEntity.getDataEntity(), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @GET
  @Path("{id}/comments")
  @RolesAllowed("users")
  public Response getCommentsOfActivity(@Context UriInfo uriInfo) throws Exception {
    String id = RestUtils.getPathParam(uriInfo, "id");
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    int limit = RestUtils.getLimit(uriInfo);
    int offset = RestUtils.getOffset(uriInfo);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (EntityBuilder.getActivityStream(activity, currentUser) == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    List<DataEntity> commentsEntity = EntityBuilder.buildEntityFromComment(activity, uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"), offset, limit);
    CollectionEntity collectionComment = new CollectionEntity(commentsEntity, EntityBuilder.COMMENTS_TYPE, offset, limit);    
    //
    return EntityBuilder.getResponse(collectionComment, uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
  
  @POST
  @Path("{id}/comments")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  public Response postComment(@Context UriInfo uriInfo,
                               CommentEntity model) throws Exception {
    if (model == null || model.getBody() == null || model.getBody().length() == 0) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    String id = RestUtils.getPathParam(uriInfo, "id");
    
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    ExoSocialActivity activity = activityManager.getActivity(id);
    if (activity == null) {
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    if (EntityBuilder.getActivityStream(activity, currentUser) == null) { //current user doesn't have permission to view activity
      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
    }
    
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(model.getBody());
    comment.setBody(model.getBody());
    comment.setUserId(currentUser.getId());
    activityManager.saveComment(activity, comment);
    
    return EntityBuilder.getResponse(EntityBuilder.buildEntityFromActivity(activityManager.getActivity(comment.getId()), uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand")), uriInfo, RestUtils.getJsonMediaType(), Response.Status.OK);
  }
}
