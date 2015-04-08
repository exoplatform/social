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
package org.exoplatform.social.user.profile;

import java.util.List;
import java.util.Map;

import org.exoplatform.social.core.identity.model.Profile;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 26, 2015  
 */
public class ExperiencesComparator extends UserProfileComparator {
  
  private List<Map<String, String>> experiences;
  private Profile profile;

  public ExperiencesComparator(List<Map<String, String>> experiences, Profile profile) {
    this.experiences = experiences;
    this.profile = profile;
  }

  public boolean hasChanged() {
    return hasChanged(profile, Profile.EXPERIENCES, experiences);
  }
  
  public Profile getProfile() {
    return this.profile;
  }

}
