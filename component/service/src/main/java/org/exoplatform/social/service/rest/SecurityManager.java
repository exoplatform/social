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
package org.exoplatform.social.service.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * The security manager helper class for Social Rest APIs.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @author <a href="http://phuonglm.net">phuonglm</a>
 * @since 1.2.0-GA
 * @since Jun 17, 2011
 */
public class SecurityManager {

  /**
   * The logger
   */
  private static Log LOG = ExoLogger.getLogger(SecurityManager.class);

  /**
   * <p>Checks if an authenticated remoteId of user can access an existing activity.</p>
   *
   * If the authenticated identity is the one who posted that existing activity, return true.<br />
   * If the existing activity belongs to that authenticated identity's activity stream, return true.<br />
   * If the existing activity belongs to that authenticated identity's connections' activity stream, return true.<br />
   * If the existing activity belongs to a space stream that the authenticated is a space member, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param userIdentityId the authenticated identity to check
   * @param existingActivity the existing activity to check
   * @return true or false
   */
  public static boolean canAccessActivity(PortalContainer portalContainer, String userIdentityId,
                                          ExoSocialActivity existingActivity) {

    IdentityManager identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    
    Identity authenticateIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userIdentityId, false);
    
    if(authenticateIdentity == null || existingActivity == null){
      return false;
    }
    else {
      return canAccessActivity(portalContainer, authenticateIdentity, existingActivity);
    }
    
  }

  /**
   * <p>Checks if an authenticated identity can access an existing activity.</p>
   *
   * If the authenticated identity is the one who posted that existing activity, return true.<br />
   * If the existing activity belongs to that authenticated identity's activity stream, return true.<br />
   * If the existing activity belongs to that authenticated identity's connections' activity stream, return true.<br />
   * If the existing activity belongs to a space stream that the authenticated is a space member, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the authenticated identity to check
   * @param existingActivity the existing activity to check
   * @return true or false
   */
  public static boolean canAccessActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                          ExoSocialActivity existingActivity) {

    SpaceService spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);

    if(authenticatedIdentity == null || existingActivity == null){
      return false;
    }

    // My activity
    if (authenticatedIdentity.getId().equals(existingActivity.getUserId())) {
      return true;
    }

    switch (existingActivity.getActivityStream().getType()) {

      case SPACE:

        // member or manager
        String spaceName = existingActivity.getActivityStream().getPrettyId();
        Space space = spaceService.getSpaceByPrettyName(spaceName);
        List<String> allIds = new ArrayList<String>();
        allIds.addAll(Arrays.asList(space.getMembers()));
        allIds.addAll(Arrays.asList(space.getManagers()));
        if (allIds.contains(authenticatedIdentity.getRemoteId())) {
          return true;
        }

        break;

      case USER:
        return true;
    }

    return false;

  }
  
  /**
   * <p>Checks if an poster identity has the permission to post activities on an owner identity stream.</p>
   *
   * If posterIdentity is the same as ownerIdentityStream, return true.<br />
   * If ownerIdentityStream is a user identity, and poster identity is connected to owner identity stream, return true.
   * <br />
   * If ownerIdentityStream is a space identity, and poster identity is a member of that space, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity  the authenticated identity to check
   * @param ownerIdentityStream the identity of an existing activity stream.
   * @return true or false
   */
  public static boolean canPostActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                        Identity ownerIdentityStream) {
    SpaceService spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);
    
    RelationshipManager relationshipManager = (RelationshipManager) portalContainer.
                                                                    getComponentInstanceOfType(RelationshipManager.class);
    String posterID =  authenticatedIdentity.getId();
    String ownerID = ownerIdentityStream.getId();
    
    // if poserIdentity is the same as ownerIdentityStream, return true
    if(ownerID.equals(posterID)){
      return true;
    }
    
    // Check if owner identity stream is a user identity or space identity
    if(ownerIdentityStream.getProviderId().equals(SpaceIdentityProvider.NAME)){
      //if space identity, check if is a member of
      Space space = spaceService.getSpaceByPrettyName(ownerIdentityStream.getRemoteId());
      if(spaceService.isMember(space, authenticatedIdentity.getRemoteId()) ||
            spaceService.isManager(space, authenticatedIdentity.getRemoteId()) ){
        return true;
      }
    } else {
      // if user identity, check if connected
      Relationship relationship = relationshipManager.get(authenticatedIdentity, ownerIdentityStream);
      if(relationship!=null && Relationship.Type.CONFIRMED.equals(relationship.getStatus())){
        return true;
      }
    }
    return false;
  }

  
  /**
   * <p>Checks if an authenticated identity has the permission to delete an existing activity.</p>
   *
   * If the authenticated identity is the identity who creates that existing activity, return true.<br />
   * If the authenticated identity is the stream owner of that existing activity, return true. <br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the identity to check
   * @param existingActivity the existing activity
   * @return true or false
   */
  public static boolean canDeleteActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                          ExoSocialActivity existingActivity) {
    SpaceService spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);

    // My activity
    if (authenticatedIdentity.getId().equals(existingActivity.getUserId())) {
      return true;
    }

    switch (existingActivity.getActivityStream().getType()) {
      case SPACE:
        // member or manager
        String spaceName = existingActivity.getActivityStream().getPrettyId();
        Space space = spaceService.getSpaceByPrettyName(spaceName);
        List<String> allIds = new ArrayList<String>();
        allIds.addAll(Arrays.asList(space.getManagers()));
        if (allIds.contains(authenticatedIdentity.getRemoteId())) {
          return true;
        }
        break;
      case USER:
        // My stream
        if (authenticatedIdentity.getId().equals(existingActivity.getActivityStream().getId())) {
          return true;
        }
        break;
    }
    return false;
  }
  
  
  /**
   * <p>Checks if an authenticated identity has the permission to comment on an existing activity.</p>
   *
   * If commenterIdentity is the one who creates the existing activity, return true.<br />
   * If commenterIdentity is the one who is connected to existing activity's user identity, return true.<br />
   * If commenterIdentity is the one who is a member of the existing activity's space identity, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the authenticated identity to check
   * @param existingActivity the existing activity
   * @return true or false
   */
  public static boolean canCommentToActivity(PortalContainer portalContainer, Identity authenticatedIdentity,
                                       ExoSocialActivity existingActivity) {
    SpaceService spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);
    RelationshipManager relationshipManager = (RelationshipManager) portalContainer.
                                                                    getComponentInstanceOfType(RelationshipManager.class);

    if(authenticatedIdentity == null || existingActivity == null){
      return false;
    }

    // My activity
    if (authenticatedIdentity.getId().equals(existingActivity.getUserId())) {
      return true;
    }

    switch (existingActivity.getActivityStream().getType()) {

      case SPACE:

        // member or manager
        String spaceName = existingActivity.getActivityStream().getPrettyId();
        Space space = spaceService.getSpaceByPrettyName(spaceName);
        List<String> allIds = new ArrayList<String>();
        allIds.addAll(Arrays.asList(space.getMembers()));
        allIds.addAll(Arrays.asList(space.getManagers()));
        if (allIds.contains(authenticatedIdentity.getRemoteId())) {
          return true;
        }

        break;

      case USER:

        // My stream
        if (authenticatedIdentity.getId().equals(existingActivity.getActivityStream().getId())) {
          return true;
        }

        // My netword stream
        String contactId = existingActivity.getActivityStream().getId();
        Relationship relationship = relationshipManager.get(authenticatedIdentity, new Identity(contactId));
        if (relationship != null && Relationship.Type.CONFIRMED.equals(relationship.getStatus())) {
          return true;
        }        

        // User is mentioned
        if (Util.hasMentioned(existingActivity, authenticatedIdentity.getId())) {
          return true;
        }
        
        break;

    }

    return false;
  }
  
  /**
   * <p>Checks if an authenticated identity has the permission to delete an existing comment.</p>
   *
   * If authenticatedIdentity is the one who creates the existing comment, return true.<br />
   * If authenticatedIdentity is the one who create the activity for that existing comment, return true.
   * If authenticatedIdentity is the one who is the stream owner of that comment to an activity, return true.<br />
   * If authenticatedIdentity is the one who is a manager of the existing activity's space identity, return true.<br />
   * Otherwise, return false.
   *
   * @param portalContainer the specified portal container
   * @param authenticatedIdentity the authenticated identity to check
   * @param existingComment the existing comment
   * @return true or false
   */
  public static boolean canDeleteComment(PortalContainer portalContainer, Identity authenticatedIdentity,
                                         ExoSocialActivity existingComment) {
    if (authenticatedIdentity.getId().equals(existingComment.getUserId())) {
      return true;
    } else {
      return false;
    }

  }
  /**
   * <p>Gets the current logged in Identity, if not logged in return null</p>
   * @return logged in Identity or null
   * @since 1.2.2
   * @deprecated use {@link Util#getAuthenticatedUserIdentity(String)} instead.
   */
  public static Identity getAuthenticatedUserIdentity() {
    if(ConversationState.getCurrent()!=null && ConversationState.getCurrent().getIdentity() != null &&
              ConversationState.getCurrent().getIdentity().getUserId() != null){
      IdentityManager identityManager =  Util.getIdentityManager();
      String authenticatedUserRemoteId = ConversationState.getCurrent().getIdentity().getUserId(); 
      return identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUserRemoteId, false);
    } else {
      return null;
    }
  }

   /**
   * Checks if an authenticated identity could access the activity stream of an owner stream identity.
   * If the owner stream is a user identity, return true.
   * If the owner stream is a space identity, return true only if the authenticated identity is the space member.
   *
   * Note that: this can work only with access permission of user - user, user - space.
   * If there is other identity type, this will return true.
   *
   * @param portalContainer       the portal container
   * @param authenticatedIdentity the authenticated identity
   * @param ownerStream           the stream owner identity
   * @return true or false to indicate access permission
   * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
   * @since  1.2.3
   */
  public static boolean canAccessActivityStream(PortalContainer portalContainer, Identity authenticatedIdentity,
                                                Identity ownerStream) {
    if (ownerStream.getProviderId().equals(SpaceIdentityProvider.NAME)) {
      SpaceService spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);
      Space targetSpace = spaceService.getSpaceByPrettyName(ownerStream.getRemoteId());
      if (targetSpace == null) {
        LOG.warn("targetSpace is null with prettyName: " + ownerStream.getRemoteId());
        return true;
      }
      return spaceService.isMember(targetSpace, authenticatedIdentity.getRemoteId()) ||
             spaceService.isManager(targetSpace, authenticatedIdentity.getRemoteId());
    }
    return true;
  }
}
