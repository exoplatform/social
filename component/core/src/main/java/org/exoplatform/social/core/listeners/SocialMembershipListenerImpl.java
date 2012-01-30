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
package org.exoplatform.social.core.listeners;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * SocialMembershipListenerImpl is registered to OrganizationService to handle membership operation associated
 * with space's groups.
 * - When a user's membership is removed (member or manager membership) => that user membership will be removed from spaces.
 * - When a user's membership is updated (member or manager membership) -> that user membership will be added to spaces.
 *
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since Jan 11, 2012
 */
public class SocialMembershipListenerImpl extends MembershipEventListener {

  public SocialMembershipListenerImpl() {
    
  }
  
  @Override
  public void postDelete(Membership m) throws Exception {
    if (m.getGroupId().startsWith(SpaceUtils.SPACE_GROUP)) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserACL acl = (UserACL) container.getComponentInstanceOfType(UserACL.class);
      
      //only handles these memberships have types likes 'manager' 
      //and 'member', except 'validator', '*'...so on.
      SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
      Space space = spaceService.getSpaceByGroupId(m.getGroupId());
      if (acl.getAdminMSType().equalsIgnoreCase(m.getMembershipType())) {
        spaceService.setManager(space, m.getUserName(), false);
        SpaceUtils.refreshNavigation();
      } else if (SpaceUtils.MEMBER.equalsIgnoreCase(m.getMembershipType())) {
        spaceService.removeMember(space, m.getUserName());
        spaceService.setManager(space, m.getUserName(), false);
        SpaceUtils.refreshNavigation();
      }
    }
  }

  @Override
  public void postSave(Membership m, boolean isNew) throws Exception {
    //only trigger when the Organization service adds new membership to existing SpaceGroup
    if (m.getGroupId().startsWith(SpaceUtils.SPACE_GROUP)) {

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UserACL acl = (UserACL) container.getComponentInstanceOfType(UserACL.class);
      //only handles these memberships have types likes 'manager' and 'member'
      //, except 'validator', '*'...so on.
      SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
      Space space = spaceService.getSpaceByGroupId(m.getGroupId());
      //TODO A case to confirm: will we create a new space here when a new group is created via organization portlet
      if (space != null) {
        String userName = m.getUserName();
        if (acl.getAdminMSType().equalsIgnoreCase(m.getMembershipType())) {
          if (spaceService.isManager(space, userName)) {
            return;
          }
          if (spaceService.isMember(space, userName)) {
            spaceService.setManager(space, userName, true);
          } else {
            spaceService.addMember(space, userName);
            spaceService.setManager(space, userName, true);
          }
        } else if (SpaceUtils.MEMBER.equalsIgnoreCase(m.getMembershipType())) {
          if (spaceService.isMember(space, userName)) {
            return;
          }
          spaceService.addMember(space, userName);
        }
        //Refresh GroupNavigation
        SpaceUtils.refreshNavigation();
      }

    }
  }
}
