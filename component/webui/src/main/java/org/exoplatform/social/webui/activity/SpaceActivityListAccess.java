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
package org.exoplatform.social.webui.activity;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;

/**
 * SpaceActivityListAccess
 * <p></p>
 *
 * @author Zuanoc
 * @copyright eXo SEA
 * @since Sep 7, 2010
 */
public class SpaceActivityListAccess implements ListAccess<ExoSocialActivity> {
  static private final Log LOG = ExoLogger.getLogger(SpaceActivityListAccess.class);

  /** The list. */
  private Space space;
  private Identity spaceIdentity = null;
  /**
   * Instantiates a new space list access.
   *
   * @param space the list
   */
  public SpaceActivityListAccess(Space space) {
    this.space = space;
    try {
      spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getName());
    } catch (Exception e) {
      LOG.error(e);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.commons.utils.ListAccess#getSize()
   */
  public int getSize() throws Exception {
    return Utils.getActivityManager().getActivitiesCount(spaceIdentity);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.commons.utils.ListAccess#load(int, int)
   */
  public ExoSocialActivity[] load(int index, int length) throws Exception {
    final Object[] objects = Utils.getActivityManager().getActivities(spaceIdentity, index, length).toArray();
    ExoSocialActivity[] results = new ExoSocialActivity[objects.length];
    for (int i = 0; i < objects.length; i++) {
      results[i]= (ExoSocialActivity) objects[i];
    }
    return results;
  }
}