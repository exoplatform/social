/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.identity.provider;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

/**
 * A provider for identity of groups. based on OrganizationService's groups
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class GroupIdentityProvider extends IdentityProvider<Group> {

  /** The organization service. */
  private OrganizationService organizationService;

  /** The Constant NAME. */
  public final static String  NAME = "group";

  private static final Log    LOG  = ExoLogger.getExoLogger(GroupIdentityProvider.class);

  public GroupIdentityProvider(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Group findByRemoteId(String remoteId) {
    Group group;
    try {
      GroupHandler groupHandler = organizationService.getGroupHandler();
      group = groupHandler.findGroupById(remoteId);
    } catch (Exception e) {
      LOG.error("Could not find group " + remoteId);
      return null;
    }
    return group;
  }

  @Override
  public Identity createIdentity(Group group) {
    Identity identity = new Identity(NAME, group.getId());
    return identity;
  }

  @Override
  public void populateProfile(Profile profile, Group group) {
    profile.setProperty(Profile.FIRST_NAME, group.getLabel());
    profile.setProperty(Profile.USERNAME, group.getGroupName());
  }
}
