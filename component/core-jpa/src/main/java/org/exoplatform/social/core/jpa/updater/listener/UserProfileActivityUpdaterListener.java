/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.updater.listener;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.storage.api.IdentityStorage;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 1, 2015  
 */
public class UserProfileActivityUpdaterListener extends Listener<ExoSocialActivity, String> {
  
  private static final Log LOG = ExoLogger.getLogger(UserProfileActivityUpdaterListener.class);
  
  private static final String PROFILE_ACTIVITY_TYPE = "USER_PROFILE_ACTIVITY";

  @Override
  public void onEvent(Event<ExoSocialActivity, String> event) throws Exception {
    ExoSocialActivity activity = event.getSource();
    String newActivityId = event.getData();
    IdentityStorage identityStorage = CommonsUtils.getService(IdentityStorage.class);
    if (PROFILE_ACTIVITY_TYPE.equals(activity.getType())) {
      Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, activity.getStreamOwner());
      identityStorage.updateProfileActivityId(identity, newActivityId, Profile.AttachedActivityType.USER);
      LOG.info(String.format("Migration the profile activity '%s' with old id's %s and new id's %s", activity.getTitle(), activity.getId(), newActivityId));
    }
    
  }

}
