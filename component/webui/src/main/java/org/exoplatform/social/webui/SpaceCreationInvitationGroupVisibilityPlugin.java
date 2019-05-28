/**
 * Copyright (C) 2019 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.webui;

import org.exoplatform.portal.config.GroupVisibilityPlugin;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpacesAdministrationService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of GroupVisibilityPlugin for space creation which allows to
 * see a group if any of these conditions is fulfilled:
 * * the given user is the super user
 * * the given user is a platform administrator
 * * the given user is a manager of the group
 * * the group is a space group and the given user is a spaces administrator
 */
public class SpaceCreationInvitationGroupVisibilityPlugin extends GroupVisibilityPlugin {

  public static final String          MANAGER = "manager";

  public static final String          ANY     = "*";

  private UserACL                     userACL;

  private SpacesAdministrationService spacesAdministrationService;

  private List<String>                spacesAdministratorsGroups;

  public SpaceCreationInvitationGroupVisibilityPlugin(UserACL userACL, SpacesAdministrationService spacesAdministrationService) {
    this.userACL = userACL;
    this.spacesAdministrationService = spacesAdministrationService;
  }

  @Override
  public boolean hasPermission(Identity userIdentity, Group group) {
    if (userACL.getSuperUser().equals(userIdentity.getUserId())
        || userIdentity.getMemberships()
                       .stream()
                       .anyMatch(userMembership -> userMembership.getGroup().equals(userACL.getAdminGroups()))) {
      return true;
    }

    Collection<MembershipEntry> userMemberships = userIdentity.getMemberships();
    if (spacesAdministratorsGroups == null) {
      spacesAdministratorsGroups = spacesAdministrationService.getSpacesAdministratorsMemberships()
                                                              .stream()
                                                              .map(membershipEntry -> membershipEntry.getGroup())
                                                              .collect(Collectors.toList());
    }
    return userMemberships.stream()
                          .anyMatch(userMembership -> ((group.getId().startsWith("/spaces/")
                              || group.getId().equals("/spaces"))
                              && (spacesAdministratorsGroups.contains(userMembership.getGroup())
                                  || userMembership.getGroup().equals(group.getId())
                                  || userMembership.getGroup().startsWith(group.getId() + "/")))
                              || ((userMembership.getGroup().equals(group.getId())
                                  || userMembership.getGroup().startsWith(group.getId() + "/"))
                                  && (userMembership.getMembershipType().equals(ANY)
                                      || userMembership.getMembershipType().equals(MANAGER))));
  }
}
