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

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 18, 2012  
 */
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;


/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 17, 2012  
 */
public enum SpaceAccessType {

  NOT_ADMINISTRATOR("social.space.access.not-administrator") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      
      //
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserACL acl = (UserACL) container.getComponentInstanceOfType(UserACL.class);
      //SiteKey siteKey = new SiteKey(pcontext.getSiteType(), pcontext.getSiteName());
      return acl.getSuperUser().equals(remoteId);
    }
  },
  INVITED_SPACE("social.space.access.invited-space") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return getSpaceService().isInvitedUser(space, remoteId);
    }
  },
  REQUESTED_JOIN_SPACE("social.space.access.requested-join-space") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return getSpaceService().isPendingUser(space, remoteId);
    }
  },
  PRIVATE_SPACE("social.space.access.private-space") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return false;
    }
    
  },
  NOT_MEMBER_SPACE("social.space.access.not-member-space") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return !getSpaceService().isMember(space, remoteId);
    }
    
  },
  SPACE_NOT_FOUND("social.space.access.space-not-found") {

    @Override
    public boolean doCheck(String remoteId, Space space) {
      return space == null;
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
  
  public final static String ACCESS_TYPE_KEY = "social.space.access.type.key";
  public final static String NODE_REDIRECT = "space-access";
  
  public abstract boolean doCheck(String remoteId, Space space);
}


