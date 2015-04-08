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
package org.exoplatform.social.core.profile;

import org.exoplatform.social.common.lifecycle.LifeCycleListener;

/**
 * Listen to updates on profiles.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface ProfileListener extends LifeCycleListener<ProfileLifeCycleEvent> {
  
  /**
   * about me of the profile is updated
   * @param event
   */
  public void aboutMeUpdated(ProfileLifeCycleEvent event) ;

  /**
   * avatar picture of the profile is updated
   * @param event
   */
  public void avatarUpdated(ProfileLifeCycleEvent event) ;

  /**
   * basic account info of the profile are updated
   * @param event
   */
  public void basicInfoUpdated(ProfileLifeCycleEvent event);

  /**
   * contact information of the profile is updated
   * @param event
   */
  public void contactSectionUpdated(ProfileLifeCycleEvent event) ;

  /**
   * experience section of the profile is updated
   * @param event
   */
  public void experienceSectionUpdated(ProfileLifeCycleEvent event);

  /**
   * header section of the profile is updated
   * @param event
   */
  public void headerSectionUpdated(ProfileLifeCycleEvent event) ;
  
  /**
   * new profile created
   * @param event
   */
  public void createProfile(ProfileLifeCycleEvent event) ;

}
