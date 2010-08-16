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
package org.exoplatform.social.webui.composer;

import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.application.PeopleService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.activity.UIDefaultActivity;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay.DisplayMode;
import org.exoplatform.social.webui.space.UISpaceActivitiesDisplay;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;

/**
 * Created by The eXo Platform SAS
 * Author : Zun
 *          exo@exoplatform.com
 * Jul 23, 2010
 */
@ComponentConfig()
public class UIDefaultActivityComposer extends UIActivityComposer {

  public UIDefaultActivityComposer() {
    setReadyForPostingActivity(true);
  }

  @Override
  public void onPostActivity(PostContext postContext, UIComponent source, WebuiRequestContext requestContext, String postedMessage) throws Exception {
    if (postedMessage.equals("")) {
      UIApplication uiApplication = requestContext.getUIApplication();
      uiApplication.addMessage(new ApplicationMessage("UIComposer.msg.error.Empty_Message",
                                                    null,
                                                    ApplicationMessage.WARNING));
      return;
    }
    String remoteUser = requestContext.getRemoteUser();
    final UIComposer uiComposer = (UIComposer) source;
    ActivityManager activityManager = uiComposer.getApplicationComponent(ActivityManager.class);
    IdentityManager identityManager = uiComposer.getApplicationComponent(IdentityManager.class);
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteUser);
    String ownerName = null;
    if(postContext == UIComposer.PostContext.SPACE){
      UISpaceActivitiesDisplay uiDisplaySpaceActivities = (UISpaceActivitiesDisplay) getActivityDisplay();
      Space space = uiDisplaySpaceActivities.getSpace();

      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                               space.getId(),
                                                               false);
      Activity activity = new Activity(userIdentity.getId(),
                                   SpaceService.SPACES_APP_ID,
                                   postedMessage,
                                   null);
      activity.setType(UIDefaultActivity.ACTIVITY_TYPE);
      activityManager.saveActivity(spaceIdentity, activity);
    } else if(postContext == PostContext.USER){
      UIUserActivitiesDisplay uiUserActivitiesDisplay = (UIUserActivitiesDisplay) getActivityDisplay();
      ownerName = uiUserActivitiesDisplay.getOwnerName();
      Identity ownerIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                   ownerName);
      Activity activity = new Activity(userIdentity.getId(),
                                       PeopleService.PEOPLE_APP_ID,
                                       postedMessage,
                                       null);
      activity.setType(UIDefaultActivity.ACTIVITY_TYPE);
      activityManager.saveActivity(ownerIdentity, activity);
      uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.MY_STATUS);
    }
  }

  @Override
  protected void onClose(Event<UIActivityComposer> event) {
  }

  @Override
  protected void onSubmit(Event<UIActivityComposer> event) {
  }

  @Override
  protected void onActivate(Event<UIActivityComposer> event) {
  }
}