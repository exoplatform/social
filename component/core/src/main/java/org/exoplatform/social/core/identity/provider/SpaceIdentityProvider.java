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
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * provides identity for a space
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class SpaceIdentityProvider extends IdentityProvider<Space> {

  private static final Log LOG = ExoLogger.getExoLogger(SpaceIdentityProvider.class);

  public static final String NAME = "space";

  private SpaceService spaceService;


  public SpaceIdentityProvider(SpaceService spaceService) {
    this.spaceService = spaceService;
  }

  public Space findByRemoteId(String spaceId) {
    Space space;
    try {
      space = spaceService.getSpaceById(spaceId);

      // attempt to find by name
      if (space ==null) {
        String name = spaceId;
        if (spaceId.contains(":")) {
          name = spaceId.split(":")[1];
        }

        return spaceService.getSpaceByName(name);
      }
    } catch (Exception e) {
      LOG.error("Could not find space " + spaceId, e);
      return null;
    }
    return space;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Identity createIdentity(Space space) {
    Identity identity = new Identity(NAME, space.getId());
    return identity;
  }

  @Override
  public void populateProfile(Profile profile, Space space) {
    profile.setProperty(Profile.FIRST_NAME, space.getName());
    profile.setProperty(Profile.USERNAME, space.getGroupId());
    profile.setProperty(Profile.AVATAR_URL, space.getImageSource());
    profile.setProperty(Profile.URL, space.getImageSource());
  }
}
