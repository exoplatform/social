/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.core.space;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;


public enum SpaceAccessType {

  SUPER_ADMINISTRATOR("social.space.access.administrator") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      //
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserACL acl = (UserACL) container.getComponentInstanceOfType(UserACL.class);
      return acl.getSuperUser().equals(remoteId) && (space != null);
    }
  },
  INVITED_SPACE("social.space.access.invited-space") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return getSpaceService().isInvitedUser(space, remoteId);
    }
  }, //waiting to validate from space manager
  REQUESTED_JOIN_SPACE("social.space.access.requested-join-space") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return getSpaceService().isPendingUser(space, remoteId);
    }
  },
  CLOSED_SPACE("social.space.access.closed-space") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return !getSpaceService().isMember(space, remoteId) && "close".equals(space.getRegistration());
    }
    
  },
  JOIN_SPACE("social.space.access.join-space") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return !getSpaceService().isMember(space, remoteId) && "open".equals(space.getRegistration());
    }
    
  }, //request to join space validation
  REQUEST_JOIN_SPACE("social.space.access.request-join-space") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return !getSpaceService().isMember(space, remoteId) && "validation".equals(space.getRegistration());
    }
    
  },
  NO_AUTHENTICATED("social.space.access.no-authenticated") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return remoteId == null;
    }
    
  },
  SPACE_NOT_FOUND("social.space.access.space-not-found") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      boolean result = false;
      if (space == null) {
        result = true;
      } else if (space != null) {
        result = "hidden".equals(space.getVisibility()) && !getSpaceService().isMember(space, remoteId);
      }
      
      return result;
    }
  },
  NOT_ACCESS_WIKI_SPACE("social.space.access.not-access-wiki-space") {

      @Override
      public boolean doCheck(String remoteId, Space space) {
        //
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        UserACL acl = (UserACL) container.getComponentInstanceOfType(UserACL.class);
        //boolean isAdminGroup = acl.isUserInGroup(acl.getAdminGroups());
        boolean isSuperAdmin = acl.getSuperUser().equals(remoteId);
        
        return !getSpaceService().isMember(space, remoteId) && !isSuperAdmin;
      }
    
  };
  
  private final String name;
  
  private SpaceAccessType(String name) {
    this.name = name;
  }
  
  private static SpaceService getSpaceService() {
    return (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
  }
  
  @Override
  public String toString() {
    return name;
  }
  
  public final static String ACCESSED_TYPE_KEY = "social.accessed.space.type.key";
  public final static String ACCESSED_SPACE_PRETTY_NAME_KEY = "social.accessed.space.key";
  public final static String ACCESSED_SPACE_WIKI_PAGE_KEY = "social.accessed.space.wiki.page.key";
  public final static String ACCESSED_SPACE_REQUEST_PATH_KEY = "social.accessed.space.request.path.key";
  public final static String NODE_REDIRECT = "space-access";
  
  public abstract boolean doCheck(String remoteId, Space space);
  
  
}

