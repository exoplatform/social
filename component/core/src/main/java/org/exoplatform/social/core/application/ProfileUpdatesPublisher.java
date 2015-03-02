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

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.container.PortalContainer;
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
import org.exoplatform.social.core.storage.api.IdentityStorage;

import java.util.LinkedHashMap;
import java.util.Map;


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
  
  public static final String USER_POSITION_PARAM = "USER_POSITION_PARAM";
  public static final String POSITION_TITLE_ID = "position_updated";
  
  private static final Log LOG = ExoLogger.getLogger(ProfileUpdatesPublisher.class);
  private ActivityManager activityManager;
  private IdentityManager identityManager;

  public ProfileUpdatesPublisher(InitParams params, ActivityManager activityManager, IdentityManager identityManager) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
  }

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage ="Avatar has been updated.";
    publishActivity(event, activityMessage, "avatar_updated");
  }


  @Override
  public void basicInfoUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage ="Basic informations has been updated.";
    publishActivity(event, activityMessage, "basic_info_updated");
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage = "Contact informations has been updated.";
    publishActivity(event, activityMessage, "contact_section_updated");
  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage = "Experiences has been updated.";
    publishActivity(event, activityMessage, "experience_section_updated");
  }

  @Override
  public void headerSectionUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage = "Position is now: " + StringEscapeUtils.unescapeHtml(event.getProfile().getPosition());
    publishActivity(event, activityMessage, "position_updated");
  }
  
  private ExoSocialActivity createComment(String title, String titleId, Identity identity, String position) {
    ExoSocialActivityImpl comment = new ExoSocialActivityImpl();
    comment.setTitle(title);
    comment.setUserId(identity.getId());
    comment.setType(PeopleService.PEOPLE_APP_ID);
    comment.setTitleId(titleId);
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    if (POSITION_TITLE_ID.equals(titleId)) {
      templateParams.put(USER_POSITION_PARAM, StringEscapeUtils.unescapeHtml(position));
      templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, USER_POSITION_PARAM);
    }
    comment.setTemplateParams(templateParams);
    return comment;
  }

  private void publishActivity(ProfileLifeCycleEvent event, String activityMessage, String titleId) {
    String activityId = getStorage().getProfileActivityId(event.getProfile(), Profile.AttachedActivityType.USER);
    ExoSocialActivityImpl activity = (ExoSocialActivityImpl) activityManager.getActivity(activityId);
    if (activity == null) {
      activity = new ExoSocialActivityImpl();
      activity.setType(PeopleService.USER_PROFILE_ACTIVITY);
      activityId = null;
    }
    activity.setTitle(activityMessage);
    publish(event, activity, activityId, titleId);
  }

  private void publish(ProfileLifeCycleEvent event, ExoSocialActivity activity, String activityId, String titleId) {
    Profile profile = event.getProfile();
    Identity identity = profile.getIdentity();
    try {
      reloadIfNeeded(identity);
      if (activityId == null) {
        activityManager.saveActivityNoReturn(identity, activity);
        getStorage().updateProfileActivityId(identity, activity.getId(), Profile.AttachedActivityType.USER);
      }  
      ExoSocialActivity comment = createComment(activity.getTitle(), titleId, identity, event.getProfile().getPosition());
      activityManager.saveComment(activity, comment);
      
    } catch (Exception e) {
      LOG.warn("Failed to publish event " + event + ": " + e.getMessage());
    }
  }

  private void reloadIfNeeded(Identity id1) throws Exception {
    if (id1.getId() == null || id1.getProfile().getFullName().length() == 0) {
      id1 = identityManager.getIdentity(id1.getGlobalId().toString(), true);
    }
  }
  
  private IdentityStorage getStorage() {
    return (IdentityStorage) PortalContainer.getInstance().getComponentInstanceOfType(IdentityStorage.class);
  }

  @Override
  public void createProfile(ProfileLifeCycleEvent event) {
  }

  @Override
  public void aboutMeUpdated(ProfileLifeCycleEvent event) {
    final String activityMessage = "About me has been updated.";
    publishActivity(event, activityMessage, "aboutMe_section_updated");
    
  }
  
}
