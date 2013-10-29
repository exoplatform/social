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

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.application.PeopleService;
import org.exoplatform.social.core.application.SpaceActivityPublisher;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
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

@ComponentConfig()
public class UIDefaultActivityComposer extends UIActivityComposer {

  public UIDefaultActivityComposer() {
    setReadyForPostingActivity(false);
  }

  @Override
  public void onPostActivity(PostContext postContext, UIComponent source,
                             WebuiRequestContext requestContext, String postedMessage) throws Exception {
    if (postedMessage.equals("")) {
      UIApplication uiApplication = requestContext.getUIApplication();
      uiApplication.addMessage(new ApplicationMessage("UIComposer.msg.error.Empty_Message",
                                                    null,
                                                    ApplicationMessage.WARNING));
      return;
    }

    if (postContext == UIComposer.PostContext.SPACE){
      UISpaceActivitiesDisplay uiDisplaySpaceActivities = (UISpaceActivitiesDisplay) getActivityDisplay();
      Space space = uiDisplaySpaceActivities.getSpace();

      Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                               space.getPrettyName(),
                                                               false);
      ExoSocialActivity activity = new ExoSocialActivityImpl(Utils.getViewerIdentity().getId(),
                                   SpaceActivityPublisher.SPACE_APP_ID,
                                   postedMessage,
                                   null);
      activity.setType(UIDefaultActivity.ACTIVITY_TYPE);
      Utils.getActivityManager().saveActivityNoReturn(spaceIdentity, activity);
      uiDisplaySpaceActivities.init();
    } else if (postContext == PostContext.USER) {
      UIUserActivitiesDisplay uiUserActivitiesDisplay = (UIUserActivitiesDisplay) getActivityDisplay();
      Identity ownerIdentity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                   uiUserActivitiesDisplay.getOwnerName(), false);
      ExoSocialActivity activity = new ExoSocialActivityImpl(Utils.getViewerIdentity().getId(),
                                       PeopleService.PEOPLE_APP_ID,
                                       postedMessage,
                                       null);
      activity.setType(UIDefaultActivity.ACTIVITY_TYPE);
      Utils.getActivityManager().saveActivityNoReturn(ownerIdentity, activity);

      //
      //uiUserActivitiesDisplay.setPostActivity(true);

      if (uiUserActivitiesDisplay.getSelectedDisplayMode() == DisplayMode.MY_SPACE) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.ALL_ACTIVITIES);
      }
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
