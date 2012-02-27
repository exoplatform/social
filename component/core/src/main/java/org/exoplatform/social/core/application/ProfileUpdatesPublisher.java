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
package org.exoplatform.social.core.application;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;


/**
 * Publish updates onto the user's activity stream when his profile is updated.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ProfileUpdatesPublisher extends ProfileListenerPlugin {

  /**
   * The USER_NAME_PARAM template param key
   * @since 1.2.8
   */
  public static final String USER_NAME_PARAM = "USER_NAME_PARAM";

  private static final Log LOG = ExoLogger.getLogger(ProfileUpdatesPublisher.class);
  private ActivityManager activityManager;
  private IdentityManager identityManager;

  public ProfileUpdatesPublisher(InitParams params, ActivityManager activityManager, IdentityManager identityManager) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
  }

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage = "@" + event.getUsername() + " has an updated profile picture.";
    publishActivity(event, activityMessage, "avatar_updated");
  }


  @Override
  public void basicInfoUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage = "@" + event.getUsername() + " has an updated basic profile info.";
    publishActivity(event, activityMessage, "basic_info_updated");
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage = "@" + event.getUsername() + " has an updated contact info.";
    publishActivity(event, activityMessage, "contact_section_updated");
  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage = "@" + event.getUsername() + " has an updated experience section.";
    publishActivity(event, activityMessage, "experience_section_updated");
  }

  @Override
  public void headerSectionUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage = "@" + event.getUsername() + " has an updated header info.";
    publishActivity(event, activityMessage, "header_section_updated");
  }

  private void publishActivity(ProfileLifeCycleEvent event, String activityMessage, String titleId) {
    ExoSocialActivityImpl activity = new ExoSocialActivityImpl();
    activity.setType(PeopleService.PEOPLE_APP_ID);
    activity.setTitleId(titleId);
    activity.setTitle(activityMessage);
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put(USER_NAME_PARAM, "@" + event.getUsername());
    templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, USER_NAME_PARAM);
    activity.setTemplateParams(templateParams);
    publish(event, activity);
  }

  private void publish(ProfileLifeCycleEvent event, ExoSocialActivity activity) {
    Profile profile = event.getProfile();
    Identity identity = profile.getIdentity();
    try {
      reloadIfNeeded(identity);
      activityManager.saveActivityNoReturn(identity, activity);
    } catch (Exception e) {
      LOG.warn("Failed to publish event " + event + ": " + e.getMessage());
    }
  }

  private void reloadIfNeeded(Identity id1) throws Exception {
    if (id1.getId() == null || id1.getProfile().getFullName().length() == 0) {
      id1 = identityManager.getIdentity(id1.getGlobalId().toString(), true);
    }
  }

}
