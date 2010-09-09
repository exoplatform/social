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
package org.exoplatform.social.webui.profile;

import org.apache.commons.lang.Validate;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.activity.UserActivityListAccess;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Displays user's activities
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jul 30, 2010
 * @copyright eXo SAS
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/profile/UIUserActivitiesDisplay.gtmpl",
  events = {
    @EventConfig(listeners = UIUserActivitiesDisplay.ChangeDisplayModeActionListener.class)
  }
)
public class UIUserActivitiesDisplay extends UIContainer {

  static private final Log      LOG = ExoLogger.getLogger(UIUserActivitiesDisplay.class);
  private static final int      ACTIVITY_PER_PAGE = 10;

  public enum DisplayMode {
    CONNECTIONS,
    SPACES,
    MY_STATUS,
    OWNER_STATUS
  }
  private DisplayMode selectedDisplayMode = DisplayMode.CONNECTIONS;

  private String                ownerName;
  private String                viewerName;
  private boolean               isActivityStreamOwner = false;

  private UIActivitiesContainer uiActivitiesContainer;
  private UIPageIterator pageIterator;
  /**
   * constructor
   */
  public UIUserActivitiesDisplay() {

  }

  public boolean isActivityStreamOwner() {
    return isActivityStreamOwner;
  }

  public void setSelectedDisplayMode(DisplayMode displayMode) {
    selectedDisplayMode = displayMode;
    try {
      init();
    } catch (Exception e) {
      LOG.error("Failed to init()");
    }
  }

  public DisplayMode getSelectedDisplayMode() {
    return selectedDisplayMode;
  }
  /**
   * sets activity stream owner (user remote Id)
   *
   * @param ownerName
   * @throws Exception
   */
  public void setOwnerName(String ownerName) throws Exception {
    this.ownerName = ownerName;
    viewerName = PortalRequestContext.getCurrentInstance().getRemoteUser();
    isActivityStreamOwner = viewerName.equals(ownerName);
    if (!isActivityStreamOwner) {
      selectedDisplayMode = DisplayMode.OWNER_STATUS;
    }
    init();
  }

  public String getOwnerName() {
    return ownerName;
  }

  public static class ChangeDisplayModeActionListener extends EventListener<UIUserActivitiesDisplay> {

    @Override
    public void execute(Event<UIUserActivitiesDisplay> event) throws Exception {
      UIUserActivitiesDisplay uiUserActivitiesDisplay = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String selectedDisplayMode = requestContext.getRequestParameter(OBJECTID);
      if (selectedDisplayMode.equals(DisplayMode.MY_STATUS.toString())) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.MY_STATUS);
      } else if (selectedDisplayMode.equals(DisplayMode.SPACES.toString())) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.SPACES);
      } else {
        uiUserActivitiesDisplay.setSelectedDisplayMode(DisplayMode.CONNECTIONS);
      }
      requestContext.addUIComponentToUpdateByAjax(uiUserActivitiesDisplay);
    }

  }
  /**
   * initialize
   *
   * @throws Exception
   */
  public void init() throws Exception {
    Validate.notNull(ownerName, "ownerName must not be null.");
    Validate.notNull(viewerName, "viewerName must not be null.");

    removeChild(UIPageIterator.class);
    pageIterator = addChild(UIPageIterator.class, null, "UIActivitiesPageIterator");
    
    removeChild(UIActivitiesContainer.class);
    uiActivitiesContainer = addChild(UIActivitiesContainer.class, null, null);
    uiActivitiesContainer.setPostContext(PostContext.USER);
    uiActivitiesContainer.setOwnerName(ownerName);
  }

  private void bindDataToActivitiesContainer() throws Exception {
    int currentPage = pageIterator.getCurrentPage();
    Identity ownerIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, ownerName);
    LazyPageList<Activity> pageList = new LazyPageList<Activity>(new UserActivityListAccess(ownerIdentity,getSelectedDisplayMode()), ACTIVITY_PER_PAGE);
    pageIterator.setPageList(pageList);
    int pageCount = pageIterator.getAvailablePage();
    if (pageCount >= currentPage) {
      pageIterator.setCurrentPage(currentPage);
    } else if (pageCount < currentPage) {
      pageIterator.setCurrentPage(currentPage - 1);
    }

    uiActivitiesContainer.setActivityList(pageIterator.getCurrentPageData());
  }

  /**
   * Gets identityManager
   *
   * @return
   */
  private IdentityManager getIdentityManager() {
    return getApplicationComponent(IdentityManager.class);
  }
}
