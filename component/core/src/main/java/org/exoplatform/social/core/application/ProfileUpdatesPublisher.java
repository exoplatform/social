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
import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.UpdateType;
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
  private static final String BREAKLINE_STR = "<br />";
  private ActivityManager activityManager;
  private IdentityManager identityManager;

  enum ActivityMessages {
    POSITION(UpdateType.POSITION, "Position is now: "),
    BASIC_INFOR(UpdateType.BASIC_INFOR, "Basic informations has been updated."),
    CONTACT(UpdateType.CONTACT, "Contact informations has been updated."),
    EXPERIENCES(UpdateType.EXPERIENCES, "Experiences has been updated."),
    AVATAR(UpdateType.AVATAR, "Avatar has been updated."),
    ABOUT_ME(UpdateType.ABOUT_ME, "About me has been updated.");

    private final UpdateType key;
    private final String msg;
    
    ActivityMessages(UpdateType key, String msg) {
      this.key = key;
      this.msg = msg;
    }
    UpdateType getKey() {
      return this.key;
    }
    public String getMsg() {
      return msg;
    }
    public static String getActivityMessage(UpdateType key) {
      for (ActivityMessages am : ActivityMessages.values()) {
        if (am.getKey().equals(key)) {
          return am.getMsg();
        }
      }
      return StringUtils.EMPTY;
    }
  }
  
  public ProfileUpdatesPublisher(InitParams params, ActivityManager activityManager, IdentityManager identityManager) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
  }

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    publishActivity(event, "avatar_updated");
  }


  @Override
  public void basicInfoUpdated(ProfileLifeCycleEvent event) {
    publishActivity(event, "basic_info_updated");
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    publishActivity(event, "contact_section_updated");
  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
    publishActivity(event, "experience_section_updated");
  }

  @Override
  public void headerSectionUpdated(ProfileLifeCycleEvent event) {
    publishActivity(event, "position_updated");
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

  private void publishActivity(ProfileLifeCycleEvent event, String titleId) {
    String activityId = getStorage().getProfileActivityId(event.getProfile(), Profile.AttachedActivityType.USER);
    ExoSocialActivityImpl activity = (ExoSocialActivityImpl) activityManager.getActivity(activityId);
    if (activity == null) {
      activity = new ExoSocialActivityImpl();
      activity.setType(PeopleService.USER_PROFILE_ACTIVITY);
      activityId = null;
    }
    
    String existingActivityTitle = activity.getTitle();
    StringBuilder sb = new StringBuilder();
    String breakLine = StringUtils.EMPTY;
    for (UpdateType ut : event.getProfile().getListUpdateTypes()) {
      sb.append(breakLine);
      sb.append(ActivityMessages.getActivityMessage(ut));
      breakLine = BREAKLINE_STR;
    }
    String newActivityTitle = sb.toString();
    activity.setTitle(newActivityTitle);
    boolean hasUpdated = newActivityTitle.replaceAll(BREAKLINE_STR, "").length() > 0
      && !newActivityTitle.equals(existingActivityTitle);
    if (activityId != null && hasUpdated) {
      activityManager.updateActivity(activity);
    }
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
    publishActivity(event, "aboutMe_section_updated");
  }
  
}
